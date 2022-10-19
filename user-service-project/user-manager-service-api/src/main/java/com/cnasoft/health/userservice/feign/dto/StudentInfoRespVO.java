package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ganghe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("学生详情返回结果")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentInfoRespVO {

    @ApiModelProperty(value = "学生id，编辑时用")
    private Long id;

    @ApiModelProperty(value = "学生姓名")
    private String name;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "曾用名")
    private String usedName;

    @ApiModelProperty(value = "身份证号")
    private String identityCardNumber;

    @ApiModelProperty(value = "性别: 1: 男 2: 女")
    private Integer sex;

    @ApiModelProperty(value = "生日,时间戳")
    @JsonSerialize(using = DateSerializer.class)
    private Date birthday;

    @ApiModelProperty(value = "民族，1：汉族；2：彝族；3：回族；4：藏族；5：蒙古族；6：满族；7：维吾尔族；8：苗族；9：壮族；10：土家族；11：其他")
    private String nation;

    @ApiModelProperty(value = "籍贯")
    private String nativePlace;

    @ApiModelProperty(value = "出生地")
    private String birthPlace;

    @ApiModelProperty(value = "政治面貌，1：中共党员；2：共青团员；3：民主党派；4：群众；5：其他")
    private Integer politicsStatus;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "班级")
    private Long clazzId;

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    @ApiModelProperty(value = "学籍号")
    private String studentCode;

    @ApiModelProperty(value = "学籍状态：1=在读，2=毕业，3=转校，4=休学，5=退学，6=肄业")
    private Integer studentStatus;

    @JsonIgnore
    private Long parentId;

    @ApiModelProperty(value = "家长姓名")
    private String parentName;

    @ApiModelProperty(value = "家长手机")
    private String parentMobile;

    @ApiModelProperty(value = "家长关系")
    private Integer relationship;

    @ApiModelProperty(value = "学生电话")
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
    private List<Integer> familyConditions = new ArrayList<>();
}