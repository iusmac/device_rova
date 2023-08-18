package lineageos.trust;

public class TrustInterface {
    /**
     * Trust warning: Public Key build signature
     *
     * When {@link #TRUST_FEATURE_KEYS} is not {@link #TRUST_FEATURE_LEVEL_GOOD}
     * notify the user about the issue
     *
     * @see #postNotificationForFeature
     */

    public static final int TRUST_WARN_PUBLIC_KEY = 1 << 2;
    /**
     * Trust warning: SELinux
     *
     * When {@link #TRUST_FEATURE_SELINUX} is not {@link #TRUST_FEATURE_LEVEL_GOOD}
     * notify the user about the issue
     *
     * @see #postNotificationForFeature
     */

    public static final int TRUST_WARN_SELINUX = 1;
    /**
     * Max / default value for warnings status
     *
     * Includes all the TRUST_WARN_
     *
     * @see #postNotificationForFeature
     * @hide
     */
    public static final int TRUST_WARN_MAX_VALUE =
            TRUST_WARN_SELINUX |
            TRUST_WARN_PUBLIC_KEY;
}
