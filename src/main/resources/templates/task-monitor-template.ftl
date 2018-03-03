<!DOCTYPE html>
<html>
<head>
    <title>定时任务监控 ${date} </title>
</head>
<body>
<h2>定时任务监控 ${date} </h2>
<br/>

<div><b>一、异常任务</b></div>
<div>
    <table border="0" cellpadding="0" cellspacing="0" width="1426" style="border-collapse:
 collapse;width:1068pt">

        <colgroup>
            <col width="98" span="2" style="mso-width-source:userset;mso-width-alt:3498;
 width:74pt">
            <col width="90" style="mso-width-source:userset;mso-width-alt:3185;width:67pt">
            <col width="136" style="mso-width-source:userset;mso-width-alt:4835;width:102pt">
            <col width="106" style="mso-width-source:userset;mso-width-alt:3783;width:80pt">
            <col width="87" style="mso-width-source:userset;mso-width-alt:3100;width:65pt">
            <col width="92" style="mso-width-source:userset;mso-width-alt:3271;width:69pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="99" style="mso-width-source:userset;mso-width-alt:3527;width:74pt">
            <col width="138" style="mso-width-source:userset;mso-width-alt:4892;width:103pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="94" style="mso-width-source:userset;mso-width-alt:3328;width:70pt">
            <col width="92" span="2" style="mso-width-source:userset;mso-width-alt:3271;
 width:69pt">
        </colgroup>
        <tbody>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: 宋体, sans-serif; vertical-align: middle;font-family: 宋体; white-space: nowrap; text-align: center; background-color: rgb(234, 141, 134);">
        <td height="18" class="xl72" width="98"
            style="border: 0.5pt solid windowtext; height: 13.2pt; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px; ">
            任务id
        </td>
        <td class="xl73" width="98"
            style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            任务名称
        </td>
        <td class="xl73" width="90"
            style="border: 0.5pt solid windowtext; width: 67pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            存储过程名称
        </td>
        <td class="xl74" width="136"
            style="border: 0.5pt solid windowtext; width: 102pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            数据库
        </td>
        <td class="xl73" width="106"
            style="border: 0.5pt solid windowtext; width: 80pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            分组
        </td>
        <td class="xl73" width="87"
            style="border: 0.5pt solid windowtext; width: 65pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            组内排序
        </td>
        <td class="xl73" width="92"
            style="border: 0.5pt solid windowtext; width: 69pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次执行时间
        </td>
        <td class="xl73" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次扫描时间
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            状态
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            周期
        </td>
        </tr>
        <#list red as o>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: right;">
            <td height="18" class="xl79"
                style="height: 13.2pt; border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: middle; white-space: nowrap; text-align: right;">${o.taskId}</td><#--任务id-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.taskName}</td><#--任务名称-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                <#if o.procedureName??>
                    ${o.procedureName}
                </#if>
            </td><#--存储过程名称-->
            <td class="xl81"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.dbType}</td><#--数据库-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.group}</td><#--分组-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.order}</td><#--组内排序-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executeNextStr}</td><#--下次执行时间-->
            <td class="xl84"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.scanNext?string('yyyy-MM-dd HH:mm:ss')}</td> <#--下次扫描时间-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.statusStr}</td><#--状态-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executePeriod}${o.executeTypeStr}</td><#--扫描周期 + 扫描类型-->
        </tr>
        </#list>
        </tbody>
    </table>
</div>

<br/>
<div><b>二、正在执行(执行时间超过${overtime}分钟)</b></div>
<div>
    <table border="0" cellpadding="0" cellspacing="0" width="1426" style="border-collapse:
 collapse;width:1068pt">

        <colgroup>
            <col width="98" span="2" style="mso-width-source:userset;mso-width-alt:3498;
 width:74pt">
            <col width="90" style="mso-width-source:userset;mso-width-alt:3185;width:67pt">
            <col width="136" style="mso-width-source:userset;mso-width-alt:4835;width:102pt">
            <col width="106" style="mso-width-source:userset;mso-width-alt:3783;width:80pt">
            <col width="87" style="mso-width-source:userset;mso-width-alt:3100;width:65pt">
            <col width="92" style="mso-width-source:userset;mso-width-alt:3271;width:69pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="99" style="mso-width-source:userset;mso-width-alt:3527;width:74pt">
            <col width="138" style="mso-width-source:userset;mso-width-alt:4892;width:103pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="94" style="mso-width-source:userset;mso-width-alt:3328;width:70pt">
            <col width="92" span="2" style="mso-width-source:userset;mso-width-alt:3271;
 width:69pt">
        </colgroup>
        <tbody>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: 宋体, sans-serif; vertical-align: middle;font-family: 宋体; white-space: nowrap; text-align: center; background-color: rgb(255, 235, 57);">
        <td height="18" class="xl72" width="98"
            style="border: 0.5pt solid windowtext; height: 13.2pt; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px; ">
            任务id
        </td>
        <td class="xl73" width="98"
            style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            任务名称
        </td>
        <td class="xl73" width="90"
            style="border: 0.5pt solid windowtext; width: 67pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            存储过程名称
        </td>
        <td class="xl74" width="136"
            style="border: 0.5pt solid windowtext; width: 102pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            数据库
        </td>
        <td class="xl73" width="106"
            style="border: 0.5pt solid windowtext; width: 80pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            分组
        </td>
        <td class="xl73" width="87"
            style="border: 0.5pt solid windowtext; width: 65pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            组内排序
        </td>
        <td class="xl73" width="92"
            style="border: 0.5pt solid windowtext; width: 69pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次执行时间
        </td>
        <td class="xl73" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次扫描时间
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            状态
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            周期
        </td>
        </tr>
        <#list yellow as o>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: right;">
            <td height="18" class="xl79"
                style="height: 13.2pt; border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: middle; white-space: nowrap; text-align: right;">${o.taskId}</td><#--任务id-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.taskName}</td><#--任务名称-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                <#if o.procedureName??>
                    ${o.procedureName}
                </#if>
            </td><#--存储过程名称-->
            <td class="xl81"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.dbType}</td><#--数据库-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.group}</td><#--分组-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.order}</td><#--组内排序-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executeNextStr}</td><#--下次执行时间-->
            <td class="xl84"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.scanNext?string('yyyy-MM-dd HH:mm:ss')}</td> <#--下次扫描时间-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.statusStr}</td><#--状态-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executePeriod}${o.executeTypeStr}</td><#--扫描周期 + 扫描类型-->
        </tr>
        </#list>
        </tbody>
    </table>
</div>
<br/>
<div><b>三、正在执行</b></div>
<div>
    <table border="0" cellpadding="0" cellspacing="0" width="1426" style="border-collapse:
 collapse;width:1068pt">

        <colgroup>
            <col width="98" span="2" style="mso-width-source:userset;mso-width-alt:3498;
 width:74pt">
            <col width="90" style="mso-width-source:userset;mso-width-alt:3185;width:67pt">
            <col width="136" style="mso-width-source:userset;mso-width-alt:4835;width:102pt">
            <col width="106" style="mso-width-source:userset;mso-width-alt:3783;width:80pt">
            <col width="87" style="mso-width-source:userset;mso-width-alt:3100;width:65pt">
            <col width="92" style="mso-width-source:userset;mso-width-alt:3271;width:69pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="99" style="mso-width-source:userset;mso-width-alt:3527;width:74pt">
            <col width="138" style="mso-width-source:userset;mso-width-alt:4892;width:103pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="94" style="mso-width-source:userset;mso-width-alt:3328;width:70pt">
            <col width="92" span="2" style="mso-width-source:userset;mso-width-alt:3271;
 width:69pt">
        </colgroup>
        <tbody>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: 宋体, sans-serif; vertical-align: middle;font-family: 宋体; white-space: nowrap; text-align: center; background-color: rgb(122, 196, 255);">
        <td height="18" class="xl72" width="98"
            style="border: 0.5pt solid windowtext; height: 13.2pt; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px; ">
            任务id
        </td>
        <td class="xl73" width="98"
            style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            任务名称
        </td>
        <td class="xl73" width="90"
            style="border: 0.5pt solid windowtext; width: 67pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            存储过程名称
        </td>
        <td class="xl74" width="136"
            style="border: 0.5pt solid windowtext; width: 102pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            数据库
        </td>
        <td class="xl73" width="106"
            style="border: 0.5pt solid windowtext; width: 80pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            分组
        </td>
        <td class="xl73" width="87"
            style="border: 0.5pt solid windowtext; width: 65pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            组内排序
        </td>
        <td class="xl73" width="92"
            style="border: 0.5pt solid windowtext; width: 69pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次执行时间
        </td>
        <td class="xl73" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次扫描时间
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            状态
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            周期
        </td>
        </tr>
        <#list blue as o>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: right;">
            <td height="18" class="xl79"
                style="height: 13.2pt; border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: middle; white-space: nowrap; text-align: right;">${o.taskId}</td><#--任务id-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.taskName}</td><#--任务名称-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                <#if o.procedureName??>
                    ${o.procedureName}
                </#if>
            </td><#--存储过程名称-->
            <td class="xl81"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.dbType}</td><#--数据库-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.group}</td><#--分组-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.order}</td><#--组内排序-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executeNextStr}</td><#--下次执行时间-->
            <td class="xl84"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.scanNext?string('yyyy-MM-dd HH:mm:ss')}</td> <#--下次扫描时间-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.statusStr}</td><#--状态-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executePeriod}${o.executeTypeStr}</td><#--扫描周期 + 扫描类型-->
        </tr>
        </#list>
        </tbody>
    </table>
</div>
<br/>
<div><b>四、正常启用</b></div>
<div>
    <table border="0" cellpadding="0" cellspacing="0" width="1426" style="border-collapse:
 collapse;width:1068pt">

        <colgroup>
            <col width="98" span="2" style="mso-width-source:userset;mso-width-alt:3498;
 width:74pt">
            <col width="90" style="mso-width-source:userset;mso-width-alt:3185;width:67pt">
            <col width="136" style="mso-width-source:userset;mso-width-alt:4835;width:102pt">
            <col width="106" style="mso-width-source:userset;mso-width-alt:3783;width:80pt">
            <col width="87" style="mso-width-source:userset;mso-width-alt:3100;width:65pt">
            <col width="92" style="mso-width-source:userset;mso-width-alt:3271;width:69pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="99" style="mso-width-source:userset;mso-width-alt:3527;width:74pt">
            <col width="138" style="mso-width-source:userset;mso-width-alt:4892;width:103pt">
            <col width="102" style="mso-width-source:userset;mso-width-alt:3612;width:76pt">
            <col width="94" style="mso-width-source:userset;mso-width-alt:3328;width:70pt">
            <col width="92" span="2" style="mso-width-source:userset;mso-width-alt:3271;
 width:69pt">
        </colgroup>
        <tbody>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: 宋体, sans-serif; vertical-align: middle;font-family: 宋体; white-space: nowrap; text-align: center; background-color: rgb(198, 224, 180);">
        <td height="18" class="xl72" width="98"
            style="border: 0.5pt solid windowtext; height: 13.2pt; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px; ">
            任务id
        </td>
        <td class="xl73" width="98"
            style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            任务名称
        </td>
        <td class="xl73" width="90"
            style="border: 0.5pt solid windowtext; width: 67pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            存储过程名称
        </td>
        <td class="xl74" width="136"
            style="border: 0.5pt solid windowtext; width: 102pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            数据库
        </td>
        <td class="xl73" width="106"
            style="border: 0.5pt solid windowtext; width: 80pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            分组
        </td>
        <td class="xl73" width="87"
            style="border: 0.5pt solid windowtext; width: 65pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            组内排序
        </td>
        <td class="xl73" width="92"
            style="border: 0.5pt solid windowtext; width: 69pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次执行时间
        </td>
        <td class="xl73" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            下次扫描时间
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            状态
        </td>
        <td class="xl77" width="102"
            style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
            周期
        </td>
        </tr>
        <#list green as o>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: right;">
            <td height="18" class="xl79"
                style="height: 13.2pt; border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: middle; white-space: nowrap; text-align: right;">${o.taskId}</td><#--任务id-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.taskName}</td><#--任务名称-->
            <td class="xl80"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                <#if o.procedureName??>
                    ${o.procedureName}
                </#if>
            </td><#--存储过程名称-->
            <td class="xl81"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.dbType}</td><#--数据库-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.group}</td><#--分组-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.order}</td><#--组内排序-->
            <td class="xl82"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executeNextStr}</td><#--下次执行时间-->
            <td class="xl84"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.scanNext?string('yyyy-MM-dd HH:mm:ss')}</td> <#--下次扫描时间-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.statusStr}</td><#--状态-->
            <td class="xl85"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.executePeriod}${o.executeTypeStr}</td><#--扫描周期 + 扫描类型-->
        </tr>
        </#list>

        </tbody>
    </table>
</div> <!-- 整体情况-->

<br/>
<br/>
<br/>
</body>
</html>