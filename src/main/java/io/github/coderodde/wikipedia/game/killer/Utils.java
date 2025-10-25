package io.github.coderodde.wikipedia.game.killer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public final class Utils {
    
    private Utils() {
        
    }
    
    public static boolean isValidLanguageCode(final String languageCode) {
        return Arrays.asList(Locale.getISOLanguages())
                     .contains(languageCode);
    }
    
    public static String getRandomLanguageCode(final Random random) {
        final String[] codes = Locale.getISOLanguages();
        return codes[random.nextInt(codes.length)];
    }
}
