package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankLength;
import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ApiModel("添加/编辑学生请求参数")
public class StudentSaveVO implements Serializable {
    public interface Add {
    }

    public interface Update {
    }

    @ApiModelProperty(value = "学生id，编辑时用")
    @NotNull(groups = Update.class, message = "学生id不能为空")
    private Long id;

    @ApiModelProperty(value = "学生姓名，长度限制2~20位")
    @NotNull(message = "姓名不能为空", groups = {UserReqVO.Add.class})
    @NotBlankLength(max = 20, min = 2, message = "姓名长度限制2~20位")
    private String name;

    @ApiModelProperty(value = "昵称，长度限制20位")
    @NotBlankLength(max = 20, message = "昵称长度限制20位")
    private String nickname;

    @ApiModelProperty(value = "曾用名，长度限制2~20位")
    @NotBlankLength(max = 20, min = 2, message = "曾用名长度限制2~20位")
    private String usedName;

    @ApiModelProperty(value = "身份证号")
    @NotNull(message = "身份证号不能为空", groups = {UserReqVO.Add.class})
    @Pattern(regexp = TextValidator.REGEX_ID, message = "身份证号格式错误")
    private String identityCardNumber;

    @ApiModelProperty(value = "所有的民族，长度限制10位")
    @NotBlankLength(max = 10, message = "民族长度限制10位")
    private String nation;

    @ApiModelProperty(value = "籍贯，长度限制100位")
    @NotBlankLength(max = 100, message = "籍贯长度限制100位")
    private String nativePlace;

    @ApiModelProperty(value = "出生地，长度限制100位")
    @NotBlankLength(max = 100, message = "出生地长度限制100位")
    private String birthPlace;

    @ApiModelProperty(value = "政治面貌，1：中共党员；2：共青团员；3：民主党派；4：群众；5：其他")
    private Integer politicsStatus;

    @ApiModelProperty(value = "年级")
    @NotNull(message = "年级不能为空", groups = {UserReqVO.Add.class, UserReqVO.Update.class})
    private String grade;

    @ApiModelProperty(value = "班级")
    @NotNull(message = "班级不能为空", groups = {UserReqVO.Add.class})
    private Long clazzId;

    @ApiModelProperty(value = "学号，长度限制100位")
    @NotBlankLength(max = 100, message = "学号长度限制100位")
    private String studentNumber;

    @ApiModelProperty(value = "学籍号，长度限制100位")
    @NotBlankLength(max = 100, message = "学籍号长度限制100位")
    private String studentCode;

    @ApiModelProperty(value = "学籍状态")
    private Integer studentStatus;

    @JsonIgnore
    private Long parentId;

    @ApiModelProperty(value = "家长姓名，长度限制2~20位")
    @NotBlankLength(max = 20, min = 2, message = "家长姓名长度限制2~20位")
    private String parentName;

    @ApiModelProperty(value = "家长手机")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "家长手机号格式错误")
    private String parentMobile;

    @ApiModelProperty(value = "家长关系")
    private Integer relationship;

    @ApiModelProperty(value = "true：解绑家长关系；false：不解绑家长关系")
    private Boolean isUnbindParent;

    @ApiModelProperty(value = "学生电话")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "学生手机号格式错误")
    private String mobile;

    @ApiModelProperty(value = "电子邮箱")
    private String email;

    @ApiModelProperty(value = "国籍，国籍，1：中国；0：其他")
    private Integer country;

    @ApiModelProperty(value = "血型，1：A型；2：B型；3：O型；4：AB型")
    private Integer bloodType;

    @ApiModelProperty(value = "健康状况，1：未填；2：健康或良好；3：一般或较弱；4：有慢性病；5：有生理缺陷；6：残疾；：7其他")
    private Integer healthyStatus;

    @ApiModelProperty(value = "港澳台侨外，1：是；0：否")
    private Boolean gatqw;

    @ApiModelProperty(value = "家中排行，1：大子女；2：中间子女；3：最小子女")
    private Integer familySort;

    @ApiModelProperty(value = "疾病史")
    @NotBlankLength(max = 255, message = "疾病史内容过长")
    private String illnessHistory;

    @ApiModelProperty(value = "身高（cm）")
    private Integer height;

    @ApiModelProperty(value = "体重（kg）")
    private Integer weight;

    @ApiModelProperty(value = "是否是独生子女，1：是；0：否")
    private Boolean isOnly;

    @ApiModelProperty(value = "残疾类型；1：无残疾；2：视力残疾；3：听力残疾；4：言语残疾；5：肢体残疾；6：智力残疾；7：精神残疾")
    private Integer disabilityType;

    @ApiModelProperty(value = "是否是烈士或优抚子女，1：是；0：否")
    private Boolean isChildOfMartyrEntitled;

    @ApiModelProperty(value = "是否进城务工随迁，1：是；0：否")
    private Boolean isChildOfMigrant;

    @ApiModelProperty(value = "是否申请资助，1：是；0：否")
    private Boolean isSubsidized;

    @ApiModelProperty(value = "是否是孤儿，1：是；0：否")
    private Boolean isOrphan;

    @ApiModelProperty(value = "是否留守儿童，1：非留守儿童；2：单亲留守儿童；3：双亲留守儿童")
    private Integer isLeft;

    @ApiModelProperty(value = "特俗情况分类：1：辍学:；2：复读；3：贫困生；4：家庭情况特殊；5：其他")
    private Integer specialCondition;

    @ApiModelProperty(value = "家庭情况分类，1：正常；2：单亲；3：离异再婚；4：丧亡学生；5：非婚子女；6：父母服刑7：吸毒人员家庭子女")
    private List<Integer> familyConditions;
}
