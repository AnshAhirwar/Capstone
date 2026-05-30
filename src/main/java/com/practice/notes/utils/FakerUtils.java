package com.practice.notes.utils;

import com.github.javafaker.Faker;

public class FakerUtils {
    private static final Faker faker = new Faker();

    public static String generateEmail() {
        return faker.internet().emailAddress();
    }

    public static String generatePassword() {
        return faker.internet().password(6, 12, true, true, true);
    }

    public static String generateName() {
        return faker.name().fullName();
    }

    public static String generateNoteTitle() {
        return faker.book().title();
    }

    public static String generateNoteDescription() {
        return faker.lorem().paragraph(2);
    }
}
