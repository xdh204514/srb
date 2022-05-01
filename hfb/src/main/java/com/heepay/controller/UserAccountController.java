package com.heepay.controller;

import com.heepay.model.NotifyVo;
import com.heepay.service.UserAccountService;
import com.heepay.service.UserBindService;
import com.heepay.task.ScheduledTask;
import com.heepay.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author qy
 *
 */
@CrossOrigin
@Controller
@RequestMapping("/userAccount")
@Slf4j
public class UserAccountController {

	@Resource
	private UserAccountService userAccountService;

	@Resource
	private UserBindService userBindService;

	@Resource
	private ThreadPoolExecutor threadPoolExecutor;

	/**
	 * 充值
	 * @param request
	 * @return
	 */
	@PostMapping("/AgreeBankCharge")
	public String  bind(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> paramMap = SignUtil.switchMap(request.getParameterMap());
		SignUtil.isSignEquals(paramMap);

		model.addAttribute("paramMap", paramMap);
		model.addAttribute("userBind", userBindService.getByBindCode((String)paramMap.get("bindCode")));
		// 返回汇付宝项目中的 index.html 页面，在该页面中填写充值金额，然后提交表单到 /userAccount/recharge 地址
		return "userAccount/index";
	}

	@PostMapping("/recharge")
	public String recharge(Model model,HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 1. 获取提交参数(在 index.html 页面可以看出具体提交了哪些参数)
		Map<String, Object> paramMap = SignUtil.switchMap(request.getParameterMap());

		// 2. 根据 bindCode 获取到用户并检查支付密码是否一致
		userBindService.checkPassword((String)paramMap.get("bindCode"), request.getParameter("payPasswd"));

		// 3. 对该账户进行充值
		userAccountService.recharge(paramMap);

		// 4. 组装参数返回给商户平台(尚融宝)
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode","0001");  // 实际生产项目中是需要获取与银行对接口后得到的返回值，判断银行卡余额是否修改成功
		resultMap.put("resultMsg","充值成功");
		resultMap.put("agentBillNo",paramMap.get("agentBillNo"));
		resultMap.put("bindCode",paramMap.get("bindCode"));
		resultMap.put("chargeAmt", new BigDecimal((String)paramMap.get("chargeAmt")));
		resultMap.put("mchFee",new BigDecimal("0"));
		resultMap.put("hyFee",new BigDecimal("0"));
		resultMap.put("timestamp",new Date().getTime());
		resultMap.put("sign",SignUtil.getSign(resultMap));

		//异步通知
		//threadPoolExecutor.submit(new NotifyThread((String)paramMap.get("notifyUrl"), paramMap));
		// NotifyVo 对象包含异步通知地址(notifyUrl)和异步通知返回参数(resultMap)两个部分
		// 使用 ScheduledTask 完成异步任务
		ScheduledTask.queue.offer(new NotifyVo((String)paramMap.get("notifyUrl"), resultMap));

		//同步跳转
		//response.sendRedirect(userBind.getReturnUrl());
		model.addAttribute("returnUrl", paramMap.get("returnUrl"));
		return "userAccount/success";
	}

	/**
	 * 提现
	 * @param request
	 * @return
	 */
	@PostMapping("/CashBankManager")
	public String  CashBankManager(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> paramMap = SignUtil.switchMap(request.getParameterMap());
		SignUtil.isSignEquals(paramMap);

		model.addAttribute("paramMap", paramMap);
		model.addAttribute("userBind", userBindService.getByBindCode((String)paramMap.get("bindCode")));
		return "withdraw/index";
	}

	@PostMapping("/withdraw")
	public String withdraw(Model model,HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> paramMap = SignUtil.switchMap(request.getParameterMap());

		userBindService.checkPassword((String)paramMap.get("bindCode"), request.getParameter("payPasswd"));

		userAccountService.withdraw(paramMap);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode","0001");
		resultMap.put("resultMsg","充值成功");
		resultMap.put("agentBillNo",paramMap.get("agentBillNo"));
		resultMap.put("bindCode",paramMap.get("bindCode"));
		resultMap.put("fetchAmt", new BigDecimal((String)paramMap.get("fetchAmt")));
		resultMap.put("mchFee",new BigDecimal("0"));
		resultMap.put("timestamp",new Date().getTime());
		resultMap.put("sign",SignUtil.getSign(resultMap));

		//异步通知
		//threadPoolExecutor.submit(new NotifyThread((String)paramMap.get("notifyUrl"), paramMap));
		ScheduledTask.queue.offer(new NotifyVo((String)paramMap.get("notifyUrl"), resultMap));

		//同步跳转
		//response.sendRedirect(userBind.getReturnUrl());
		model.addAttribute("returnUrl", paramMap.get("returnUrl"));
		return "withdraw/success";
	}
}

