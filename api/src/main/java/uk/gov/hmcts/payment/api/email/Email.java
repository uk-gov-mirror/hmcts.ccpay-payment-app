package uk.gov.hmcts.payment.api.email;

import lombok.*;

import java.util.List;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@Builder(builderMethodName = "emailWith")
public class Email {

    private String from;
    private String[] to;
    private String subject;
    private String message;
    private List<EmailAttachment> attachments;

    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }
}
