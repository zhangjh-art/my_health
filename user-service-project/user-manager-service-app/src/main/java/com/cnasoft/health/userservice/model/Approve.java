package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 审核记录表
 * @author zcb
 * @TableName approve
 */
@TableName("approve")
@Data
@EqualsAndHashCode(callSuper = false)
public class Approve extends SuperModel<Approve> {
    @TableId
    private Long id;

    /**
     * 审核类型，1：工作权限审核；2：角色审核；3：区域审核；4：学校审核；5：部门审核；6：量表审核；7：账号审核；8：资源审核；9：测试管理员审核
     */
    private Integer approveType;

    /**
     * 操作，1：新增；2：编辑；3：删除
     */
    private Integer operation;

    /**
     * 审核状态，0：待审核；1：通过；2：拒绝
     */
    private Integer approveStatus;

    /**
     * 业务id，例如角色id，工作权限id，区域id
     */
    private Long businessId;

    /**
     * 审批时间
     */
    private Date approveTime;

    /**
     * 审批人user_id
     */
    private Long approveUserId;

    /**
     * 审批时备注
     */
    private String approveRemark;

    /**
     * 修改后数据json字符串，编辑时需要
     */
    private String afterString;

    /**
     * 修改前数据json字符串，编辑时需要
     */
    private String beforeString;

}