package uk.gov.hmcts.payment.api.service;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.payment.api.dto.FeePayApportionCCDCase;
import uk.gov.hmcts.payment.api.model.*;
import uk.gov.hmcts.payment.api.util.DateFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class FeePayApportionServiceImpl implements FeePayApportionService {

    private static final Logger LOG = LoggerFactory.getLogger(FeePayApportionServiceImpl.class);

    private final PaymentFeeLinkRepository paymentFeeLinkRepository;

    private final PaymentStatusRepository paymentStatusRepository;

    private final FeePayApportionRepository feePayApportionRepository;

    private final PaymentFeeRepository paymentFeeRepository;

    private final DateFormatter dateFormatter;

    private final String APPORTION_TYPE_AUTO = "AUTO";

    private final String APPORTION_TYPE_MANUAL = "MANUAL";

    private Boolean isSurplus = false;

    public FeePayApportionServiceImpl(PaymentFeeLinkRepository paymentFeeLinkRepository,
                                      PaymentStatusRepository paymentStatusRepository,
                                      FeePayApportionRepository feePayApportionRepository,
                                      PaymentFeeRepository paymentFeeRepository, DateFormatter dateFormatter) {
        this.paymentFeeLinkRepository = paymentFeeLinkRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.feePayApportionRepository = feePayApportionRepository;
        this.paymentFeeRepository = paymentFeeRepository;
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void updateFeeAmountDue(Payment payment) {
        Optional<List<FeePayApportion>> apportions = feePayApportionRepository.findByPaymentId(payment.getId());
        if(apportions.isPresent()) {
            apportions.get().stream()
                .forEach(feePayApportion -> {
                    PaymentFee fee = paymentFeeRepository.findById(feePayApportion.getFeeId()).get();
                    if(feePayApportion.getCallSurplusAmount() == null) {
                        feePayApportion.setCallSurplusAmount(BigDecimal.valueOf(0));
                    }
                    fee.setAmountDue(fee.getAmountDue().subtract(feePayApportion.getApportionAmount()
                        .add(feePayApportion.getCallSurplusAmount())));

                    if(payment.getPaymentChannel() != null && payment.getPaymentChannel().getName() != null &&
                        (payment.getPaymentChannel().getName().equalsIgnoreCase("telephony") ||
                            payment.getPaymentChannel().getName().equalsIgnoreCase("online"))) {
                        if(fee.getAmountDue() != null && fee.getAmountDue().compareTo(BigDecimal.valueOf(0)) < 0) {
                            feePayApportion.setCallSurplusAmount(feePayApportion.getCallSurplusAmount().subtract(fee.getAmountDue()));
                            feePayApportionRepository.save(feePayApportion);
                        }
                    }

                    paymentFeeRepository.save(fee);
                    LOG.info("Updated FeeId " + fee.getId() + " as PaymentId " + payment.getId() + " Status Changed to " + payment.getPaymentStatus().getName());
                });
        }
    }

    @Override
    public void processApportion(Payment payment) {
        Optional<List<PaymentFee>> savedFees = paymentFeeRepository.findByCcdCaseNumber(payment.getCcdCaseNumber());
        if (savedFees.isPresent()) {

            List<PaymentFee> sortedFees = savedFees.get()
                .stream()
                .filter(fee -> fee.getDateCreated() != null)
                .sorted(Comparator.comparing(PaymentFee::getDateCreated))
                .collect(Collectors.toList());
            sortedFees.stream().forEach(fee -> {
                fee.setNetAmount(fee.getNetAmount() != null
                    ? fee.getNetAmount()
                    : payment.getPaymentLink() != null ? getFeeCalculatedNetAmount(fee, payment.getPaymentLink().getRemissions())
                    : getFeeCalculatedNetAmount(fee, null));
                if (fee.getAmountDue() == null) {
                    fee.setAmountDue(fee.getNetAmount());
                }
            });
            this.processFeePayApportion(FeePayApportionCCDCase.feePayApportionCCDCaseWith()
                .ccdCaseNo(payment.getCcdCaseNumber())
                .fees(sortedFees)
                .payments(Lists.newArrayList(payment))
                .build());
        }
    }

    @Override
    public FeePayApportionCCDCase processFeePayApportion(FeePayApportionCCDCase feePayApportionCCDCase) {

        BigDecimal callSurplusAmount = BigDecimal.valueOf(0);
        BigDecimal remainingPaymentAmount;

        List<Payment> paymentsToBeApportioned = getPaymentsToBeApportioned(feePayApportionCCDCase.getPayments());
        List<PaymentFee> feesToBeApportioned = getFeesToBeApportioned(feePayApportionCCDCase.getFees());

        if (!paymentsToBeApportioned.isEmpty() && !feesToBeApportioned.isEmpty()) {
            List<FeePayApportion> feePayApportions = new ArrayList<>();

            for (Payment payment : paymentsToBeApportioned) {
                remainingPaymentAmount = payment.getAmount();

                for (PaymentFee fee : feesToBeApportioned) {
                    if (fee.getAmountDue() != null && fee.getAmountDue().compareTo(BigDecimal.valueOf(0)) > 0) {

                        FeePayApportion feePayApportion = applyFeePayApportion(fee, payment, remainingPaymentAmount);
                        feePayApportions.add(feePayApportion);

                        if (remainingPaymentAmount.compareTo(fee.getAmountDue()) > 0) {
                            remainingPaymentAmount = remainingPaymentAmount.subtract(fee.getAmountDue());
                        }else {
                            remainingPaymentAmount = BigDecimal.valueOf(0);
                        }

                        if (remainingPaymentAmount.compareTo(BigDecimal.valueOf(0)) > 0) {
                            continue;
                        } else {
                            break;
                        }
                    }
                }
                // End Fee Loop
                if (remainingPaymentAmount.compareTo(BigDecimal.valueOf(0)) > 0) {
                    callSurplusAmount = remainingPaymentAmount;
                    isSurplus = true;
                } else {
                    isSurplus = false;
                }
            }
            // End Payment Loop
            if (feePayApportions != null && !feePayApportions.isEmpty()) {
                if (isSurplus) {
                    findSurplusFee(feePayApportions, callSurplusAmount, feePayApportionCCDCase.getFees());
                }
                feePayApportions.stream().forEach(feePayApportion -> {
                    feePayApportionRepository.save(feePayApportion);
                });
                feePayApportionCCDCase.getFees().stream()
                    .forEach(fee -> {
                        paymentFeeRepository.save(fee);
                    });
            }
        }
        return feePayApportionCCDCase;
    }

    private FeePayApportion applyFeePayApportion(PaymentFee fee, Payment payment, BigDecimal remainingPaymentAmount) {

        // Create a new Record in FeePayApportion Table
        FeePayApportion feePayApportion = FeePayApportion.feePayApportionWith()
            .feeId(fee.getId())
            .paymentId(payment.getId())
            .feeAmount(fee.getNetAmount())
            .paymentAmount(payment.getAmount())
            .ccdCaseNumber(payment.getCcdCaseNumber())
            .createdBy("SYSTEM")
            .apportionType(APPORTION_TYPE_AUTO)
            .dateCreated(payment.getDateCreated())
            .build();

        if (remainingPaymentAmount.compareTo(fee.getAmountDue()) > 0) {
            feePayApportion.setApportionAmount(fee.getAmountDue());
        } else {
            feePayApportion.setApportionAmount(remainingPaymentAmount);
        }

        fee.setDateApportioned(feePayApportion.getDateCreated());

        return feePayApportion;
    }

    private BigDecimal getFeeCalculatedNetAmount(PaymentFee fee, List<Remission> remissions) {
        fee.setCalculatedAmount(fee.getCalculatedAmount() != null ? fee.getCalculatedAmount() : getFeeCalculatedAmount(fee));
        if (remissions != null && !CollectionUtils.isEmpty(remissions)) {
            remissions.stream()
                .filter(remission -> remission.getFee().getId().equals(fee.getId()))
                .forEach(remission -> fee.setCalculatedAmount(fee.getCalculatedAmount().subtract(remission.getHwfAmount())));
        }
        return fee.getCalculatedAmount();
    }

    private BigDecimal getFeeCalculatedAmount(PaymentFee fee) {
        fee.setVolume(fee.getVolume() > 0 ? fee.getVolume() : 1);
        return fee.getFeeAmount() != null ? fee.getFeeAmount().multiply(new BigDecimal(fee.getVolume())) : new BigDecimal(0);
    }


    private List<Payment> getPaymentsToBeApportioned(List<Payment> payments) {
        return payments.stream()
            .filter(payment -> payment.getAmount() != null
                && payment.getAmount().compareTo(BigDecimal.valueOf(0)) > 0)
            .collect(Collectors.toList());
    }

    private List<PaymentFee> getFeesToBeApportioned(List<PaymentFee> fees) {
        return fees.stream()
            .filter(fee -> (fee.getDateCreated() != null)
                && fee.getNetAmount() != null
                && fee.getNetAmount().compareTo(BigDecimal.valueOf(0)) > 0)
            .collect(Collectors.toList());
    }

    private void findSurplusFee(List<FeePayApportion> feePayApportions, BigDecimal callSurplusAmount, List<PaymentFee> fees) {
        FeePayApportion lastFeePayApportion = (FeePayApportion) feePayApportions.toArray()[feePayApportions.size() - 1];
        lastFeePayApportion.setCallSurplusAmount(callSurplusAmount);
    }
}
