package com.jh.paymentgateway.util.sdk.pay;


import com.jh.paymentgateway.util.sdk.domain.Response;
import com.jh.paymentgateway.util.sdk.pay.domain.WechatH5.WechatH5Request;
import com.jh.paymentgateway.util.sdk.pay.domain.WechatH5.WechatH5Response;
import com.jh.paymentgateway.util.sdk.pay.domain.aliJSAPI.AliJSAPIRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.aliJSAPI.AliJSAPIResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.authentication.AuthenticationRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.authentication.AuthenticationResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.cashierPay.CashierRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.cashierPay.CashierResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.microPay.MicroPayRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.microPay.MicroPayResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.nativePay.NativePayRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.nativePay.NativePayResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.paymenQuery.PaymentQueryRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.paymenQuery.PaymentQueryResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.protocol.*;
import com.jh.paymentgateway.util.sdk.pay.domain.refund.RefundApplyRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.refund.RefundApplyResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.refundQuery.RefundApplyQueryRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.refundQuery.RefundApplyQueryResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.split.SplitRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.split.SplitResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.split.SplitResultQueryRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.split.SplitResultQueryResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.splitRefund.RefundRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.splitRefund.RefundResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.union.UnionPayRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.union.UnionPayResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.wechatJSAPI.WechatJSAPIRequest;
import com.jh.paymentgateway.util.sdk.pay.domain.wechatJSAPI.WechatJSAPIResponse;
import com.jh.paymentgateway.util.sdk.pay.domain.withdraw.*;
import com.jh.paymentgateway.util.sdk.utils.Config;
import com.jh.paymentgateway.util.sdk.utils.RemoteInvoker;

import java.util.Map;

public class PaymentHelper {

    /**
     * 支付宝服务窗支付
     * @throws Exception
     */
    public static AliJSAPIResponse aliJSAPIPay(AliJSAPIRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getAliJSAPIUrl(), AliJSAPIResponse.class);
    }

    /**
     * 微信公众号支付
     * @throws Exception
     */
    public static WechatJSAPIResponse wechatJSAPIPay(WechatJSAPIRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getWechatJSAPIUrl(), WechatJSAPIResponse.class);
    }

    /**
     * 微信H5支付
     * @throws Exception
     */
    public static WechatH5Response wechatH5Pay(WechatH5Request request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getWechatH5Url(), WechatH5Response.class);
    }

    /**
     * 主掃支付
     * @throws Exception
     */
    public static NativePayResponse nativePay(NativePayRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getNativePayUrl(), NativePayResponse.class);
    }

    /**
     * 被掃支付
     * @throws Exception
     */
    public static MicroPayResponse microPay(MicroPayRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getScaningURL(), MicroPayResponse.class);
    }

    /**
     * 預下單支付/收銀檯支付
     * @throws Exception
     */
    public static CashierResponse cashierPay(CashierRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getCashierPayUrl(), CashierResponse.class);
    }

    /**
     * 支付结果查询
     * @throws Exception
     */
    public static PaymentQueryResponse paymentQuery(PaymentQueryRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getPaymentQueryUrl(), PaymentQueryResponse.class);
    }
    //------------------退款----------------
    /**
     * 退款申请
     * @throws Exception
     */
    @Deprecated
    public static RefundApplyResponse refundApply(RefundApplyRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getRefundApplyUrl(), RefundApplyResponse.class);
    }
    
    /**
     * 退款查询
     * @throws Exception
     */
    public static RefundApplyQueryResponse refundQuery(RefundApplyQueryRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getRefundQueryUrl(), RefundApplyQueryResponse.class);
    }

    /**
     * 退款
     * @throws Exception
     */
    public static RefundResponse refund(RefundRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getRefundUrl(), RefundResponse.class);
    }

    //------------------分账----------------
    /**
     * 分账
     * @throws Exception
     */
    public static SplitResponse split(SplitRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getSplitPayUrl(), SplitResponse.class);
    }
    
    /**
     * 分账查询
     * @throws Exception
     */
    public static SplitResultQueryResponse splitResultQuery(SplitResultQueryRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getSplitResultQueryURL(), SplitResultQueryResponse.class);
    }
    //------------------提现----------------
    /**
     * 子商户提现
     * @throws Exception
     */
    public static Withdraw4SubMerchantResponse withdraw4SubMerchant(Withdraw4SubMerchantRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getWithdraw4SubMerchantUrl(), Withdraw4SubMerchantResponse.class);
    }
    /**
     * 子商户提现查询
     * @throws Exception
     */
    public static WithdrawQuery4SubMerchantResponse withdrawQuery4SubMerchant(WithdrawQuery4SubMerchantRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getWithdrawQuery4SubMerchantUrl(), WithdrawQuery4SubMerchantResponse.class);
    }
    /**
     * 单笔代付
     * @throws Exception
     */
    public static WithdrawToCardResponse withdrawalToCard(WithdrawToCardRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getWithdrawalToCardUrl(), WithdrawToCardResponse.class);
    }
    
    /**
     * 网银支付
     * @throws Exception
     */
    public static UnionPayResponse unionPay(UnionPayRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getUnionPayUrl(), UnionPayResponse.class);
    }
    
    /**
     * 单笔代付查询
     * @throws Exception
     */
    public static WithdrawToCardQueryResponse withdrawalToCardQuery(PaymentQueryRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getWithdrawalToCardQueryUrl(), WithdrawToCardQueryResponse.class);
    }
    
    /**
     * 余额查询
     * @throws Exception
     */
    public static CustomerAccountQueryResp accountQuery(Map<String, String> request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getAccountQueryUrl(), CustomerAccountQueryResp.class);
    }
    
    /**
     * 协议支付绑卡预交易
     * @throws Exception
     */
    public static ProtocolPayBindCardResponse bindCard(ProtocolPayBindCardRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getBindCardUrl(), ProtocolPayBindCardResponse.class);
    }
    
    /**
     * 协议支付绑卡确认
     * @throws Exception
     */
    public static ProtocolPayBindCardConfirmResponse bindCardConfirm(ProtocolPayBindCardConfirmRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getBindCardConfirmUrl(), ProtocolPayBindCardConfirmResponse.class);
    }
    /**
     * 协议支付绑卡确认
     * @param request
     * @throws Exception
     */
    public static ProtocolQueryCunsumerResponse queryConsumer(ProtocolQueryConsumerRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getqueryConsumersConfirmUrl(), ProtocolQueryCunsumerResponse.class);
    }
    
    /**
     * 协议支付预交易
     * @throws Exception
     * @return
     */
    public static Response protocolPayPre(ProtocolPayRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getProtocolPayPreUrl(), ProtocolPayResponse.class);
    }
    
    /**
     * 协议支付确认交易
     * @throws Exception
     */
    public static ProtocolPayConfirmResponse protocolPayConfirm(ProtocolPayConfirmRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getProtocolPayConfirmUrl(), ProtocolPayConfirmResponse.class);
    }
    
    /**
     * 协议支付解绑
     * @throws Exception
     */
    public static ProtocolPayBindCardResponse unBindCard(ProtocolPayUnbindCardRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getUnBindCardUrl(), ProtocolPayBindCardResponse.class);
    }
    
    /**
     * 协议查询
     * @throws Exception
     */
    public static ProtocolQueryResponse protocolQuery(ProtocolQueryRequest request) throws Exception{
        return RemoteInvoker.invoke(request, Config.getProtocolQuery(), ProtocolQueryResponse.class);
    }
    
    /**
     * 银行卡鉴权
     * @throws Exception
     */
    public static AuthenticationResponse authentication(AuthenticationRequest order) throws Exception{
        return RemoteInvoker.invoke(order, Config.getAuthenticationUrl(), AuthenticationResponse.class);
    }    
    
    
}

