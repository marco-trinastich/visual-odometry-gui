/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.utilities;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.enums.core.DataSize;
import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.interfaces.WithValue;
import io.quarkus.logging.Log;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommonUtils {

    private final static int DEFAULT_MAX_DIGITS_SIZE = 1;

    // Formatters

    public static @NotNull BigDecimal roundBigDecimal(double d, int decimalPlaces) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(d));
        bigDecimal = bigDecimal.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bigDecimal;
    }

    public static String getFormattedTime(double seconds) {
        if (seconds <= 60) {
            // Set elapsed time to seconds
            return String.format(AppConstants.SECONDS_PATTERN, roundBigDecimal(seconds, 2));
        } else {
            // Calculate minutes
            int minutes = (int) seconds / 60;
            // Calculate remaining seconds
            BigDecimal remainingSeconds = roundBigDecimal(seconds % 60, 2);

            // Set elapsed time to minutes/seconds
            return String.format(AppConstants.MINUTES_PATTERN, minutes, remainingSeconds);
        }
    }

    public static int getSecsTimeDiff(long startTimeMs, long endTimeMs) {
        return (int) ((endTimeMs - startTimeMs) * 0.001);
    }

    public static @NotNull String getFormattedExponential(double value) {
        return AppConstants.EXPONENTIAL_FORMAT
                .format(value)
                .replace(AppConstants.POSITIVE_EXP, AppConstants.POSITIVE_EXP_ALT)
                .replace(AppConstants.NEGATIVE_EXP, AppConstants.NEGATIVE_EXP_ALT);
    }

    public static @NotNull Path getPath(String filename, String extension) {
        return Path.of(String.format(AppConstants.DOT_SEPARATED_PATTERN, filename, extension));
    }

    // Distance

    public static double getPointsDistance(double x1, double x2, double y1, double y2) {
        // Pythagorean theorem
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    public static double getAbsDistance(double x1, double x2) {
        return Math.abs(x1 - x2);
    }

    // Converters

    public static @NotNull String getSizeString(double bytes) {
        return getSizeString(bytes, DEFAULT_MAX_DIGITS_SIZE);
    }

    public static @NotNull String getSizeString(double bytes, int maxDigits) {
        double convertedSize = bytes;
        int digits;
        int divisions = 0;

        do {
            digits = CommonUtils.roundBigDecimal(convertedSize, 0).toString().length();
            if (digits > maxDigits) {
                convertedSize = convertedSize / DataSize.Kilobytes.size();
                divisions++;
            }
        } while (digits > maxDigits);
        String unit = DataSize.get(divisions).unit();
        String roundedSize = roundBigDecimal(convertedSize, 1)
                .toString().replace(AppConstants.EMPTY_DECIMAL, "");

        return roundedSize + unit;
    }

    public static @Nullable Pair<Integer, Integer> getResolutionComponents(@NotNull String resolution) {
        String[] parts = resolution.split(AppConstants.RESOLUTION_SEPARATOR);

        try {
            if (parts.length == 2) {
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);
                return Pair.with(width, height);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public static String getResolution(int width, int height) {
        return String.format(AppConstants.RESOLUTION_PATTERN, width, height);
    }

    /**
     * Squared euclidean distance between two resolutions: the single nearest-match
     * metric shared by GUI reconciliation and capture-time adjustment.
     */
    public static long getResolutionDistance(int width, int height, int targetWidth, int targetHeight) {
        return (long) (width - targetWidth) * (width - targetWidth)
                + (long) (height - targetHeight) * (height - targetHeight);
    }

    public static DevicePath @NotNull [] getDevicePathDescriptors(String[] pathsArray) {
        return Arrays.stream(pathsArray).map(CommonUtils::getDevicePathDescriptor)
                .toArray(DevicePath[]::new);
    }

    public static @NotNull DevicePath getDevicePathDescriptor(String deviceFullName) {
        String deviceName = getDeviceName(deviceFullName);
        return DevicePath.from(deviceName, deviceFullName);
    }

    public static @NotNull String getDeviceName(@NotNull String deviceFullName) {
        // Remove any hex number (i.e. device addresses)
        String deviceName = StringUtils.removeHexAddress(deviceFullName);
        // Remove any guid (i.e. peripheral guid)
        deviceName = StringUtils.removeGuid(deviceName);

        return deviceName;
    }

    // Enums

    public static String @NotNull [] getEnumValues(WithValue[] values) {
        return Arrays.stream(values).map(WithValue::value).toArray(String[]::new);
    }

    // Utils

    public static @NotNull String getStringArrayFirst(String[] array) {
        String firstItem = getArrayFirst(array);
        return firstItem != null ? firstItem : "";
    }

    public static <T> T getArrayFirst(T[] array) {
        return array != null && array.length > 0 ? array[0] : null;
    }

    public static <I, O> Function<I, O> getSafeGenerator(Function<I, O> generator) {
        if (generator == null)
            return null;

        return (input) -> {
            try {
                return generator.apply(input);
            } catch (Exception e) {
                Log.errorf(Messages.GENERATOR_EXCEPTION, getCallerMethod(), e.getMessage());
            }
            return null;
        };
    }

    public static <I> Consumer<I> getSafeConsumer(Consumer<I> consumer) {
        if (consumer == null)
            return null;

        return (input) -> {
            try {
                consumer.accept(input);
            } catch (Exception e) {
                Log.errorf(Messages.CONSUMER_EXCEPTION, getCallerMethod(), e.getMessage());
            }
        };
    }

    public static String getCallerMethod() {
        var stackTrace = Thread.currentThread().getStackTrace().length > 2 ?
                Thread.currentThread().getStackTrace()[3] :
                null;

        if (stackTrace == null)
            return AppConstants.EMPTY_STRING;

        return AppConstants.DOT_SEPARATED_PATTERN.formatted(stackTrace.getClassName(), stackTrace.getMethodName());
    }

    public static void runIfNotNull(Object nullable, Runnable runnable) {
        runIfNotNull(runnable, nullable);
    }

    public static void runIfNotNull(Runnable runnable, Object... nullables) {
        if (Arrays.stream(nullables).allMatch(Objects::nonNull) && runnable != null) {
            runnable.run();
        }
    }

    // Numbers

    public static @NotNull Integer getNormalizedInteger(@NotNull NumberConstraints constraints, int value, int fallbackValue) {
        return getNormalizedNumber(constraints, new BigDecimal(value), new BigDecimal(fallbackValue)).intValue();
    }

    public static @NotNull Float getNormalizedFloat(@NotNull NumberConstraints constraints, float value, float fallbackValue) {
        return getNormalizedNumber(constraints, new BigDecimal(value), new BigDecimal(fallbackValue)).floatValue();
    }

    public static @NotNull Double getNormalizedDouble(@NotNull NumberConstraints constraints, double value, double fallbackValue) {
        return getNormalizedNumber(constraints, new BigDecimal(value), new BigDecimal(fallbackValue)).doubleValue();
    }

    public static @NotNull BigDecimal getNormalizedNumber(@NotNull NumberConstraints constraints,
                                                 @NotNull BigDecimal value,
                                                 @NotNull BigDecimal fallbackValue) {
        return switch (constraints) {
            case All -> value;
            case Positive -> getPositiveNumber(value);
            case StrictlyPositive -> getStrictlyPositiveNumber(value, fallbackValue);
            case Negative -> getNegativeNumber(value);
            case StrictlyNegative -> getStrictlyNegativeNumber(value, fallbackValue);
            case NotZero -> getNotZeroNumber(value, fallbackValue);
        };
    }

    public static @NotNull BigDecimal getPositiveNumber(@NotNull BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? value.negate() : value;
    }

    public static @NotNull BigDecimal getStrictlyPositiveNumber(@NotNull BigDecimal value,
                                                       @NotNull BigDecimal fallbackValue) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            value = fallbackValue;
        } else if (value.compareTo(BigDecimal.ZERO) < 0) {
            value = value.negate();
        }
        return value;
    }

    public static @NotNull BigDecimal getNegativeNumber(@NotNull BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) > 0 ? value.negate() : value;
    }

    public static @NotNull BigDecimal getStrictlyNegativeNumber(@NotNull BigDecimal value,
                                                       @NotNull BigDecimal fallbackValue) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            value = fallbackValue;
        } else if (value.compareTo(BigDecimal.ZERO) > 0) {
            value = value.negate();
        }
        return value;
    }

    public static @NotNull BigDecimal getNotZeroNumber(@NotNull BigDecimal value, @NotNull BigDecimal fallbackValue) {
        return value.compareTo(BigDecimal.ZERO) == 0 ? fallbackValue : value;
    }

    public static @Nullable Integer tryParseInteger(String valueStr) {
        return tryParseNumber(valueStr, BigDecimal::intValue);
    }

    public static @Nullable Float tryParseFloat(String valueStr) {
        return tryParseNumber(valueStr, BigDecimal::floatValue);
    }

    public static @Nullable Double tryParseDouble(String valueStr) {
        return tryParseNumber(valueStr, BigDecimal::doubleValue);
    }

    public static <T extends Number> @Nullable T tryParseNumber(String valueStr, Function<BigDecimal, T> generator) {
        BigDecimal value = null;
        try {
            value = new BigDecimal(valueStr);
        } catch (Exception ignored) {
        }
        return value != null ? generator.apply(value) : null;
    }
}
