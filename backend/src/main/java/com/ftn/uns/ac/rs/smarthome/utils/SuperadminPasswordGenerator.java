package com.ftn.uns.ac.rs.smarthome.utils;

import org.passay.*;
import org.springframework.stereotype.Component;

import static org.springframework.beans.MethodInvocationException.ERROR_CODE;

@Component
public class SuperadminPasswordGenerator {

    public String generateSuperadminPassword() {
        PasswordGenerator gen = new PasswordGenerator();

        LengthRule lengthRule = new LengthRule();
        lengthRule.setMinimumLength(32);

        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(1);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(1);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(1);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE;
            }

            public String getCharacters() {
                return "#?!@$%^&*-";
            }
        };

        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(1);
        return gen.generatePassword(32, splCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
    }
}
