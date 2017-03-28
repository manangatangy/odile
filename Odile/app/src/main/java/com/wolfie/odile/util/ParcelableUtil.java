package com.wolfie.odile.util;

import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

public class ParcelableUtil {
    public static void writeBoolean(Parcel dest, boolean b) {
        writeBooleanObject(dest, b);
    }

    /**
     * Use same encoding as {@link #writeBooleanObject} so that methods can be used interchangeably (reading null as
     * false)
     */
    public static boolean readBoolean(Parcel in) {
        Boolean bool = readBooleanObject(in);
        return bool == null ? false : bool;
    }

    /**
     * Encoding: null ==> 0,  false ==> 1, true ==> 2
     *
     * @param dest
     * @param bool
     */
    public static void writeBooleanObject(Parcel dest, @Nullable Boolean bool) {
        dest.writeInt(bool == null ? 0 : (bool ? 2 : 1));
    }

    @Nullable
    public static Boolean readBooleanObject(Parcel in) {
        int bool = in.readInt();
        return bool == 0 ? null : (bool == 2);
    }


    public static void writeBigDecimal(Parcel dest, @Nullable BigDecimal value) {
        dest.writeString(value == null ? null : value.toPlainString());
    }

    @Nullable
    public static BigDecimal readBigDecimal(Parcel in) {
        String value = in.readString();
        return value == null ? null : new BigDecimal(value);
    }

    public static void writeEnum(Parcel dest, Enum e) {
        dest.writeString(e == null ? null : e.name());
    }

    public static <T extends Enum<T>> T readEnum(Parcel in, Class<T> enumClass) {
        String name = in.readString();
        if (name != null) {
            T result = Enum.valueOf(enumClass, name);
            return result;
        }
        return null;
    }

    public static void putEnum(Bundle bundle, Enum value) {
        if (bundle != null && value != null) {
            putEnum(bundle, value.getClass().getName(), value);
        }
    }

    public static void putEnum(Bundle bundle, String key, Enum value) {
        if (bundle != null && key != null && value != null) {
            bundle.putString(key, value.name());
        }
    }

    public static <T extends Enum<T>> T getEnum(Bundle bundle, Class<T> enumClass) {
        if (bundle != null && enumClass != null) {
            return getEnum(bundle, enumClass.getName(), enumClass);
        }
        return null;
    }

    public static <T extends Enum<T>> T getEnum(Bundle bundle, String key, Class<T> enumClass) {
        T t = null;
        if (bundle != null && key != null && enumClass != null) {
            String name = bundle.getString(key);
            if (name != null) {
                t = Enum.valueOf(enumClass, name);
            }
        }
        return t;
    }

    public static void writeDate(Parcel dest, Date date) {
        dest.writeLong(date == null ? -1 : date.getTime());
    }

    public static Date readDate(Parcel in) {
        long millis = in.readLong();
        if (millis >= 0) {
            Date date = new Date(millis);
            return date;
        }
        return null;
    }
}
