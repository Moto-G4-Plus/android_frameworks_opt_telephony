/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony.ims;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.test.suitebuilder.annotation.SmallTest;

import com.android.ims.ImsConfig;
import com.android.ims.ImsManager;
import com.android.internal.telephony.TelephonyTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Hashtable;

public class ImsManagerTest extends TelephonyTest {
    private static final String UNSET_PROVISIONED_STRING = "unset";
    private static final boolean ENHANCED_4G_MODE_DEFAULT_VAL = true;
    private static final boolean ENHANCED_4G_MODE_EDITABLE = true;
    private static final boolean WFC_IMS_ENABLE_DEFAULT_VAL = false;
    private static final boolean WFC_IMS_ROAMING_ENABLE_DEFAULT_VAL = true;
    private static final boolean VT_IMS_ENABLE_DEFAULT_VAL = true;
    private static final int WFC_IMS_MODE_DEFAULT_VAL = 2;
    private static final int WFC_IMS_ROAMING_MODE_DEFAULT_VAL = 3;

    PersistableBundle mBundle;
    @Mock
    IBinder mBinder;
    @Mock
    ImsConfigImplBase mImsConfigImplBaseMock;
    Hashtable<Integer, Integer> mProvisionedIntVals = new Hashtable<>();
    Hashtable<Integer, String> mProvisionedStringVals = new Hashtable<>();
    ImsConfigImplBase.ImsConfigStub mImsConfigStub;
    ImsConfig mImsConfig;

    private final int[] mSubId = {0};
    private int mPhoneId;

    @Before
    public void setUp() throws Exception {
        super.setUp("SubscriptionControllerTest");
        mPhoneId = mPhone.getPhoneId();
        mBundle = mContextFixture.getCarrierConfigBundle();

        doReturn(mSubId).when(mSubscriptionController).getSubId(mPhoneId);

        doReturn(mSubscriptionController).when(mBinder).queryLocalInterface(anyString());
        mServiceManagerMockedServices.put("isub", mBinder);

        mImsManagerInstances.remove(mPhoneId);

        setDefaultValues();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void setDefaultValues() {
        mBundle.putBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL,
                ENHANCED_4G_MODE_EDITABLE);
        mBundle.putBoolean(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL,
                WFC_IMS_ENABLE_DEFAULT_VAL);
        mBundle.putBoolean(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL,
                WFC_IMS_ROAMING_ENABLE_DEFAULT_VAL);
        mBundle.putInt(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT,
                WFC_IMS_MODE_DEFAULT_VAL);
        mBundle.putInt(CarrierConfigManager.KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT,
                WFC_IMS_ROAMING_MODE_DEFAULT_VAL);
        mBundle.putBoolean(CarrierConfigManager.KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL,
                ENHANCED_4G_MODE_DEFAULT_VAL);
        mBundle.putBoolean(CarrierConfigManager.KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL, true);
    }

    @Test @SmallTest
    public void testGetDefaultValues() {
        doReturn("-1").when(mSubscriptionController)
                .getSubscriptionProperty(anyInt(), anyString(), anyString());

        ImsManager imsManager = ImsManager.getInstance(mContext, mPhoneId);

        assertEquals(WFC_IMS_ENABLE_DEFAULT_VAL, imsManager.isWfcEnabledByUser());
        verify(mSubscriptionController, times(1)).getSubscriptionProperty(
                anyInt(),
                eq(SubscriptionManager.WFC_IMS_ENABLED),
                anyString());

        assertEquals(ENHANCED_4G_MODE_DEFAULT_VAL,
                imsManager.isEnhanced4gLteModeSettingEnabledByUser());
        verify(mSubscriptionController, times(1)).getSubscriptionProperty(
                anyInt(),
                eq(SubscriptionManager.ENHANCED_4G_MODE_ENABLED),
                anyString());

        assertEquals(WFC_IMS_MODE_DEFAULT_VAL, imsManager.getWfcMode(false));
        verify(mSubscriptionController, times(1)).getSubscriptionProperty(
                anyInt(),
                eq(SubscriptionManager.WFC_IMS_MODE),
                anyString());

        assertEquals(WFC_IMS_ROAMING_MODE_DEFAULT_VAL, imsManager.getWfcMode(true));
        verify(mSubscriptionController, times(1)).getSubscriptionProperty(
                anyInt(),
                eq(SubscriptionManager.WFC_IMS_ROAMING_MODE),
                anyString());

        assertEquals(VT_IMS_ENABLE_DEFAULT_VAL, imsManager.isVtEnabledByUser());
        verify(mSubscriptionController, times(1)).getSubscriptionProperty(
                anyInt(),
                eq(SubscriptionManager.VT_IMS_ENABLED),
                anyString());
    }

    @Test @SmallTest
    public void testSetValues() {
        ImsManager imsManager = ImsManager.getInstance(mContext, mPhoneId);

        imsManager.setWfcMode(ImsConfig.WfcModeFeatureValueConstants.CELLULAR_PREFERRED);
        verify(mSubscriptionController, times(1)).setSubscriptionProperty(
                eq(mSubId[0]),
                eq(SubscriptionManager.WFC_IMS_MODE),
                eq("1"));

        imsManager.setWfcMode(ImsConfig.WfcModeFeatureValueConstants.CELLULAR_PREFERRED, true);
        verify(mSubscriptionController, times(1)).setSubscriptionProperty(
                eq(mSubId[0]),
                eq(SubscriptionManager.WFC_IMS_ROAMING_MODE),
                eq("1"));

        imsManager.setVtSetting(false);
        verify(mSubscriptionController, times(1)).setSubscriptionProperty(
                eq(mSubId[0]),
                eq(SubscriptionManager.VT_IMS_ENABLED),
                eq("0"));

        // enhanced 4g mode must be editable to use setEnhanced4gLteModeSetting
        mBundle.putBoolean(CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL,
                ENHANCED_4G_MODE_EDITABLE);
        imsManager.setEnhanced4gLteModeSetting(true);
        verify(mSubscriptionController, times(1)).setSubscriptionProperty(
                eq(mSubId[0]),
                eq(SubscriptionManager.ENHANCED_4G_MODE_ENABLED),
                eq("1"));

        imsManager.setWfcSetting(true);
        verify(mSubscriptionController, times(1)).setSubscriptionProperty(
                eq(mSubId[0]),
                eq(SubscriptionManager.WFC_IMS_ENABLED),
                eq("1"));
    }
    @Test
    public void testGetProvisionedValues() throws Exception {
        ImsManager imsManager = initializeProvisionedValues();

        assertEquals(true, imsManager.isWfcProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED));

        assertEquals(true, imsManager.isVtProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.LVC_SETTING_ENABLED));

        assertEquals(true, imsManager.isVolteProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.VLT_SETTING_ENABLED));

        // If we call get again, times should still be one because the value should be fetched
        // from cache.
        assertEquals(true, imsManager.isWfcProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED));

        assertEquals(true, imsManager.isVtProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.LVC_SETTING_ENABLED));

        assertEquals(true, imsManager.isVolteProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.VLT_SETTING_ENABLED));
    }

    @Test
    public void testSetProvisionedValues() throws Exception {
        ImsManager imsManager = initializeProvisionedValues();

        assertEquals(true, imsManager.isWfcProvisionedOnDevice());
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED));

        imsManager.getConfigInterface().setProvisionedValue(
                ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED,
                ImsConfig.FeatureValueConstants.OFF);

        assertEquals(0, (int) mProvisionedIntVals.get(
                ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED));

        assertEquals(false, imsManager.isWfcProvisionedOnDevice());

        verify(mImsConfigImplBaseMock, times(1)).setProvisionedValue(
                eq(ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED),
                eq(0));
        verify(mImsConfigImplBaseMock, times(1)).getProvisionedValue(
                eq(ImsConfig.ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED));

    }

    private ImsManager initializeProvisionedValues() {
        try {
            when(mImsConfigImplBaseMock.getProvisionedValue(anyInt()))
                    .thenAnswer(invocation ->  {
                        return getProvisionedInt((Integer) (invocation.getArguments()[0]));
                    });

            when(mImsConfigImplBaseMock.setProvisionedValue(anyInt(), anyInt()))
                    .thenAnswer(invocation ->  {
                        mProvisionedIntVals.put((Integer) (invocation.getArguments()[0]),
                                (Integer) (invocation.getArguments()[1]));
                        return ImsConfig.OperationStatusConstants.SUCCESS;
                    });

            when(mImsConfigImplBaseMock.getProvisionedStringValue(anyInt()))
                    .thenAnswer(invocation ->  {
                        return getProvisionedString((Integer) (invocation.getArguments()[0]));
                    });

            when(mImsConfigImplBaseMock.setProvisionedStringValue(anyInt(), anyString()))
                    .thenAnswer(invocation ->  {
                        mProvisionedStringVals.put((Integer) (invocation.getArguments()[0]),
                                (String) (invocation.getArguments()[1]));
                        return ImsConfig.OperationStatusConstants.SUCCESS;
                    });

        } catch (RemoteException ex) {
            fail("initializeProvisionedValues failed with " + ex);
        }

        // Configure ImsConfigStub
        mImsConfigStub = new ImsConfigImplBase.ImsConfigStub(mImsConfigImplBaseMock, mContext);
        doReturn(mImsConfigStub).when(mImsConfigImplBaseMock).getIImsConfig();

        // Configure ImsConfig
        mImsConfig = new ImsConfig(mImsConfigStub, mContext);

        // Configure ImsManager
        ImsManager imsManager = ImsManager.getInstance(mContext, mPhoneId);
        try {
            replaceInstance(ImsManager.class, "mConfig", imsManager, mImsConfig);
        } catch (Exception ex) {
            fail("failed with " + ex);
        }

        return imsManager;
    }

    // If the value is ever set, return the set value. If not, return a constant value 1000.
    private int getProvisionedInt(int item) {
        if (mProvisionedIntVals.containsKey(item)) {
            return mProvisionedIntVals.get(item);
        } else {
            return ImsConfig.FeatureValueConstants.ON;
        }
    }

    // If the value is ever set, return the set value. If not, return a constant value "unset".
    private String getProvisionedString(int item) {
        if (mProvisionedStringVals.containsKey(item)) {
            return mProvisionedStringVals.get(item);
        } else {
            return UNSET_PROVISIONED_STRING;
        }
    }
}