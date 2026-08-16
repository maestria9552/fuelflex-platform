package com.fuelflex.platform.email.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceImplTest {

    @Test
    void employeeInvitationContainsConfiguredActivationLinkAndKeepsOtpOutOfUrl() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailServiceImpl service = new EmailServiceImpl(mailSender);
        Field frontendBaseUrl = EmailServiceImpl.class.getDeclaredField("frontendBaseUrl");
        frontendBaseUrl.setAccessible(true);
        frontendBaseUrl.set(service, "https://frontend.example.test/");

        service.sendEmployeeInvitation(
                "manager@example.test",
                "Ada",
                "Manager",
                "123456",
                OffsetDateTime.now().plusMinutes(30));

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        String body = messageCaptor.getValue().getText();
        assertThat(body).contains("[ Activer mon compte ]");
        assertThat(body).contains("https://frontend.example.test/activation-employe");
        assertThat(body).contains("123456");
        assertThat(body).contains("pendant 30 minutes");
        assertThat(body).doesNotContain("activation-employe?code=123456");
    }
}
