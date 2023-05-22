/*
 * This is the source code of Wallet for Android v. 1.0.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright Nikolai Kudashov, 2019-2020.
 */

package ton.coin.wallet.rlottie;

import android.content.Context;
import android.content.res.Configuration;

public class AndroidUtilities {
    public static float density = 1;

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static int dpr(float value) {
        if (value == 0) {
            return 0;
        }
        return Math.round(density * value);
    }

    public static int dp2(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.floor(density * value);
    }

    public static void checkDisplaySize(Context context, Configuration newConfiguration) {
        density = context.getResources().getDisplayMetrics().density;
    }
}
