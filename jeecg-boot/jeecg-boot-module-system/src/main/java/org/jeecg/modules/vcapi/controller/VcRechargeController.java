package org.jeecg.modules.vcapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.quartz.job.FuLuProductDataJob;
import org.jeecg.modules.shiro.vo.ResponseBean;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.impl.SysDictServiceImpl;
import org.jeecg.modules.vcapi.entity.req.CallBackReqDto;
import org.jeecg.modules.vcapi.entity.req.RechargeReqDto;
import org.jeecg.modules.vcapi.enums.RoleCodeEnum;
import org.jeecg.modules.vcapi.service.VcRechargeService;
import org.jeecg.modules.vcapi.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @description: 虚拟货币充值api接口
 * @author: Mr.Luke
 * @create: 2019-10-14 17:19
 * @Version V1.0
 */
@RestController
@Api(tags="虚拟货币充值api接口")
@RequestMapping("/vcapi/vcRecharge")
@Slf4j
public class VcRechargeController {


    private final VcRechargeService vcRechargeService;
    private final SysDictServiceImpl sysDictService;
    private final ISysUserRoleService userRoleService;

    @Autowired
    public VcRechargeController(VcRechargeService vcRechargeService, SysDictServiceImpl sysDictService,ISysUserRoleService userRoleService) {
        this.vcRechargeService = vcRechargeService;
        this.sysDictService = sysDictService;
        this.userRoleService = userRoleService;
    }


    /**
     * @Author: Mr.Luke
     * @Description: 获取业务分类
     * @Date: 17:35 2019/10/14
     * @Param: []
     * @return: org.jeecg.modules.shiro.vo.ResponseBean
     */
    @GetMapping("BizType")
    @ApiOperation("获取业务分类")
    public ResponseBean getBizType(){
        List<DictModel> list=sysDictService.queryDictItemsByCode("BizType");

        return new ResponseBean(200,"OK",list );
    }

    @GetMapping("getProduct")
    @ApiOperation("根据业务类型获取商品信息")
    public ResponseBean getProductByBizType(String bizType){
        return vcRechargeService.getProductByBizType(bizType);
    }

    @GetMapping("oyzq")
    @ApiOperation("根据业务类型获取商品信息")
    public ResponseBean oyzq(){
        /*try {*/
            FuLuProductDataJob f=new FuLuProductDataJob();
            f.fuLuProductDataJob();
            return new ResponseBean(200,"成功","OK");
       /* }catch (Exception e){
            System.err.println(e);
            return new ResponseBean(300,"出现异常","NO");
        }*/

    }


//    @GetMapping("getBalance")
//    @ApiOperation("获取账户余额")
//    public ResponseBean getUserBalance(String bizType){
//        return vcRechargeService.getUserBalance(bizType);
//    }

    @PostMapping("recharge")
    @ApiOperation("充值API接口")
    public ResponseBean recharge(@RequestBody RechargeReqDto rechargeReqDto) {
        return vcRechargeService.recharge(rechargeReqDto);
    }

    @PostMapping("rechargeForAdmin")
    @ApiOperation("专为Admin设置的充值API接口")
    public ResponseBean rechargeForAdmin(@RequestBody RechargeReqDto rechargeReqDto) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(userRoleService.getUserRole(sysUser.getUsername()).stream().filter(role->(RoleCodeEnum.ADMIN.getRoleCode().equals(role))).collect(Collectors.toList()).size()<=0){
            return new ResponseBean(400, "", "您暂时没有该操作的权限！！！");
        }else{
            return vcRechargeService.rechargeForAdmin(rechargeReqDto);
        }
    }

    @GetMapping("queryOrder")
    @ApiOperation("查询订单详情")
    public ResponseBean queryOrder(String orderNo, String bizType){
        return vcRechargeService.queryOrder(orderNo,bizType);
    }

    @GetMapping("callback")
    @ApiOperation("订单回调")
    public String callback(CallBackReqDto callBackReqDto, HttpServletRequest request){
        Enumeration enu=request.getParameterNames();
        log.info(callBackReqDto+" 获取参数：");
        while(enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            log.info(paraName+":"+request.getParameter(paraName));
        }
        return vcRechargeService.callBack(callBackReqDto);
    }

    @RequestMapping("/orderCallback")
    @ApiOperation("福禄订单回调")
    public String orderCallback(@RequestBody String str) {
        return vcRechargeService.orderCallback(str);
    }


    @GetMapping("callbackTest")
    @ApiOperation("测试订单回调效果")
    public String callbackTest(CallBackReqDto callBackReqDto){
        log.info("测试订单回调效果:"+callBackReqDto.toString());
        return "ok";
    }

    public static void main(String[] args) throws Exception {

        SortedMap<Object,Object> signMap=new TreeMap<Object,Object>();
        signMap.put("BizType","OIL");
        signMap.put("Time","131653774326942493");
        signMap.put("UserId","Test8888");
        System.out.println(SignUtil.createSign(signMap,"0CC2EC0AE5AD4C2DA0FD419D36EBA160"));

    }
}
