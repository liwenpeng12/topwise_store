LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SRC_FILES += \
	/src/com/topwise/topos/appstore/api/IAppStoreApi.aidl \
        /src/com/topwise/topos/appstore/api/ISearchCallback.aidl \
        /src/com/topwise/topos/appstore/api/IDownloadStateListener.aidl

#LOCAL_JAVA_LIBRARIES := android-support-v4

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 zookingadsdktopwisestore legacyTopWiseStore eventbus universal  supportV4 analytics


LOCAL_PACKAGE_NAME := TopWise_Store

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PACKAGE)


##################################################

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := analytics:libs/umeng-analytics-v5.6.7.jar \
    zookingadsdktopwisestore:libs/zookingadsdk.jar \
    universal:libs/universal-image-loader-1.9.5.jar \
    eventbus:libs/eventbus-3.0.0.jar \
    legacyTopWiseStore:libs/org.apache.http.legacy.jar \
    supportV4:libs/supportV4.jar

include $(BUILD_MULTI_PREBUILT)




# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
