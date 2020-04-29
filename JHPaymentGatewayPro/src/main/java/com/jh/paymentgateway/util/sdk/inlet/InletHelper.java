package com.jh.paymentgateway.util.sdk.inlet;

import com.jh.paymentgateway.util.sdk.inlet.domain.InletMerchantQueryRequest;
import com.jh.paymentgateway.util.sdk.inlet.domain.InletMerchantQueryResponse;
import com.jh.paymentgateway.util.sdk.inlet.domain.InletMerchantRequest;
import com.jh.paymentgateway.util.sdk.inlet.domain.InletMerchantResponse;
import com.jh.paymentgateway.util.sdk.utils.Config;
import com.jh.paymentgateway.util.sdk.utils.RemoteInvoker;

public class InletHelper {

    /**
     * 添加商戶
     * @throws Exception
     */
    public static InletMerchantResponse addMerchant(InletMerchantRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getAddMerchantUrl(), InletMerchantResponse.class);
    }

    /**
     * 修改商戶
     * @throws Exception
     */
    public static InletMerchantResponse modifyMerchant(InletMerchantRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getModifyMerchantUrl(), InletMerchantResponse.class);
    }
    
    /**
     * 查詢商戶
     * @param request
     * @return
     * @throws Exception
     */
    public static InletMerchantQueryResponse queryMerchantAuditInfo(InletMerchantQueryRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getInletMerchantQueryURL(), InletMerchantQueryResponse.class);
    }
}
