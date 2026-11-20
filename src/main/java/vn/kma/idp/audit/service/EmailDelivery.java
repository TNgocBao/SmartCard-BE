package vn.kma.idp.audit.service;

/** Delivery boundary: tests replace SMTP with an in-memory recipient. */
@FunctionalInterface
public interface EmailDelivery {
    void send(String destination,String subject,String body);
}
