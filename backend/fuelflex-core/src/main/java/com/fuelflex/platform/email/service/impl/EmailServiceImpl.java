package com.fuelflex.platform.email.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fuelflex.platform.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(
            String email,
            String firstName,
            String verificationCode
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("FuelFlex Platform - Vérification de votre adresse e-mail");
        message.setText(buildMessage(firstName, verificationCode));

        mailSender.send(message);
    }

    private String buildMessage(
            String firstName,
            String verificationCode
    ) {

        return """
                Bonjour %s,

                Bienvenue sur FuelFlex Platform.

                Votre code de vérification est :

                %s

                Ce code est valable pendant 30 minutes.

                Si vous n'êtes pas à l'origine de cette inscription,
                vous pouvez ignorer cet e-mail.

                FuelFlex Platform
                """
                .formatted(firstName, verificationCode);
    }
}