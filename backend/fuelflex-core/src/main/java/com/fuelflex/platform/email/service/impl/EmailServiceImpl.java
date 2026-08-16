package com.fuelflex.platform.email.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fuelflex.platform.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

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

    @Override
    public void sendEmployeeInvitation(String email, String firstName, String lastName,
            String activationCode, java.time.OffsetDateTime expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("FuelFlex Platform - Invitation d'activation");
        message.setText(buildEmployeeInvitation(firstName, lastName, activationCode, expiresAt));
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

    private String buildEmployeeInvitation(String firstName, String lastName,
            String activationCode, java.time.OffsetDateTime expiresAt) {
        String activationUrl = frontendBaseUrl.replaceAll("/+$", "") + "/activation-employe";
        return """
                Bonjour %s %s,

                Vous avez été invité(e) à rejoindre FuelFlex Platform.
                Votre compte a été créé et doit être activé avant votre première connexion.

                [ Activer mon compte ]
                %s

                Après avoir ouvert la page d'activation, saisissez manuellement le code reçu,
                puis choisissez et confirmez votre mot de passe.

                Code d'activation :

                %s

                Ce code est valable pendant 30 minutes, jusqu'au %s, et ne peut être utilisé qu'une seule fois.
                FuelFlex ne vous enverra jamais de mot de passe par e-mail.

                Si vous n'êtes pas à l'origine de cette invitation, ignorez ce message.

                FuelFlex Platform
                """.formatted(firstName, lastName, activationUrl, activationCode, expiresAt);
    }
}
