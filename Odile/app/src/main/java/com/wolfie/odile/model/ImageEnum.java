package com.wolfie.odile.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.wolfie.odile.R;

/**
 * Organises the images used as background pics.
 */
public enum ImageEnum {

    IMAGE01(R.string.im001, R.drawable.st_basils_cathedral_1),
    IMAGE02(R.string.im002, R.drawable.st_basils_cathedral_2),
    IMAGE03(R.string.im003, R.drawable.tall_trees),
    IMAGE04(R.string.im004, R.drawable.arkhangelsk_1),
    IMAGE05(R.string.im005, R.drawable.church_of_saviour_on_spilt_blood_1),
    IMAGE06(R.string.im006, R.drawable.church_of_saviour_on_spilt_blood_2),
    IMAGE07(R.string.im007, R.drawable.moscow_immaculate_conception),
    IMAGE08(R.string.im008, R.drawable.motherland_calls),
    IMAGE09(R.string.im009, R.drawable.peter_paul_fortress_spire),
    IMAGE10(R.string.im010, R.drawable.rostov_citadel),

    IMAGE11(R.string.im011, R.drawable.sevastopol_memorial_1),
    IMAGE12(R.string.im012, R.drawable.sevastopol_memorial_2),
    IMAGE13(R.string.im013, R.drawable.valaam_chapel),
    IMAGE14(R.string.im014, R.drawable.valaam_icon),
    IMAGE15(R.string.im015, R.drawable.valaam_monastery_1),
    IMAGE16(R.string.im016, R.drawable.valaam_monastery_2),
    IMAGE17(R.string.im017, R.drawable.bolshoi_theatre_moscow),
    IMAGE18(R.string.im018, R.drawable.peterhof_palace),
    IMAGE19(R.string.im019, R.drawable.moscow_grocery_store),
    IMAGE20(R.string.im020, R.drawable.lake_ladoga),

    IMAGE21(R.string.im021, R.drawable.qolsharif_mosque_kazan_tatarstan),
    IMAGE22(R.string.im022, R.drawable.resurrection_church_kremlin_rostov_veliky),
    IMAGE23(R.string.im023, R.drawable.ostankino_tower_moscow),
    IMAGE24(R.string.im024, R.drawable.church_in_uglich),
    IMAGE25(R.string.im025, R.drawable.chesme_church_st_petersburg),
    IMAGE26(R.string.im026, R.drawable.trinity_church_bellingshausen_antarctica),
    IMAGE27(R.string.im027, R.drawable.mayakovskaya_metro_moscow),
    IMAGE28(R.string.im028, R.drawable.singer_building_home_of_books_spb_1),
    IMAGE29(R.string.im029, R.drawable.singer_building_home_of_books_spb_2),
    IMAGE30(R.string.im030, R.drawable.komsomolskaya_moscow_metro),

    IMAGE31(R.string.im031, R.drawable.transfiguration_church_kizhi_island_karelia),
    IMAGE32(R.string.im032, R.drawable.scarlet_sails_st_petersburg),
    IMAGE33(R.string.im033, R.drawable.gagarin_monument_moscow),
    IMAGE34(R.string.im034, R.drawable.st_george_nykhas_uastyrdzhi_ossetia),
    IMAGE35(R.string.im035, R.drawable.motherland_calls_mamayev_kurgan),
    IMAGE36(R.string.im036, R.drawable.smolny_convent_st_petersburg),
    IMAGE37(R.string.im037, R.drawable.vladivostok_trans_siberian_station),
    IMAGE38(R.string.im038, R.drawable.suzdal_church),
    IMAGE39(R.string.im039, R.drawable.st_petersburg_rooftop_angels);

    private @StringRes int mTitleResId;
    private @DrawableRes int mImageResId;

    ImageEnum(int titleResId, int imageResId) {
        mTitleResId = titleResId;
        mImageResId = imageResId;
    }

    public @StringRes int getTitleResId() {
        return mTitleResId;
    }

    public @DrawableRes int getImageResId() {
        return mImageResId;
    }

    public static @DrawableRes int getImageId(int item) {
        ImageEnum[] imageEnums = ImageEnum.values();
        return (0 <= item && item < imageEnums.length) ? imageEnums[item].getImageResId() : -1;
    }
}
