<!DOCTYPE html>
<html>
<head>
    <title>SMSA应用监控 ${date} </title>
</head>
<body>
<h2>SMSA应用监控 ${date} </h2>
<br/>

<div><b>一、异常应用</b></div>
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
                应用名称
            </td>
            <td class="xl73" width="98"
                style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                状态监测
            </td>
            <td class="xl73" width="98"
                style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                运行情况
            </td>
            <td class="xl73" width="90"
                style="border: 0.5pt solid windowtext; width: 67pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                运行信息
            </td>
            <td class="xl74" width="136"
                style="border: 0.5pt solid windowtext; width: 102pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                数据库
            </td>
            <td class="xl73" width="106"
                style="border: 0.5pt solid windowtext; width: 80pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                数据库监测
            </td>
            <td class="xl73" width="87"
                style="border: 0.5pt solid windowtext; width: 65pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                执行测试
            </td>
            <td class="xl73" width="92"
                style="border: 0.5pt solid windowtext; width: 69pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                连接情况
            </td>
            <td class="xl73" width="102"
                style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                连接信息
            </td>
        </tr>
        <#list red as o>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; white-space: nowrap; text-align: right;">
            <td height="18" class="xl79" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="height: 13.2pt; border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: middle; white-space: nowrap; text-align: right;">${o.appServerDesc}</td><#--应用名称-->
            <td class="xl80" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.needCheck?string ("需要","不需要")}</td><#--状态监测-->
            <td class="xl80" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                <#if o.needCheck >
                     ${o.appRunning?string ("正常","未检测到应用")}
                 <#else>
                </#if>
            </td><#--运行情况-->
            <td class="xl80" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.appRunningInfo???then(o.appRunningInfo,"")}</td><#--运行信息-->
            <#if (o.dbConnectionList?? && o.dbConnectionList?size > 0)>
                <#list o.dbConnectionList as db>
                    <#if (db_index = 0)>
                        <td class="xl81"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;background-color:${db.dbException?then('#efd0ce','')}">${db.dbType}
                            :${db.desc}</td><#--数据库-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;background-color:${db.dbException?then('#efd0ce','')}">${db.needCheck?string("需要","不需要")}</td><#--需要测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;background-color:${db.dbException?then('#efd0ce','')}">${db.testEd?string("已测试","未测试")}</td><#--执行测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;background-color:${db.dbException?then('#efd0ce','')}">${db.connectDB?string("正常","无法连接")}</td><#--连接情况-->
                        <td class="xl84"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;background-color:${db.dbException?string('#efd0ce','')}">${db.msg???string(db.msg,"")}</td> <#--连接信息-->
                    </#if>
                </#list>
            <#else>
                <td class="xl81"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--数据库-->
                <td class="xl82"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--需要测试-->
                <td class="xl82"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--执行测试-->
                <td class="xl82"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--连接情况-->
                <td class="xl84"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td> <#--连接信息-->
            </#if>
        </tr>
            <#if o.dbConnectionList??>
                <#list o.dbConnectionList as db>
                    <#if (db_index > 0)>
                    <#--<#if db.dbException>-->
                    <tr height="18"
                        style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; white-space: nowrap; text-align: right;background-color:${db.dbException?then('#efd0ce','')}">
                    <#--<#else >-->
                    <#--<tr height="18"-->
                    <#--style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; white-space: nowrap; text-align: right;">-->
                    <#--</#if>-->
                        <td class="xl81"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.dbType}
                            :${db.desc}</td><#--数据库-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.needCheck?string("需要","不需要")}</td><#--需要测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.testEd?string("已测试","未测试")}</td><#--执行测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.connectDB?string("正常","无法连接")}</td><#--连接情况-->
                        <td class="xl84"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.msg???string(db.msg,"")}</td> <#--连接信息-->
                    </tr>
                    </#if>
                </#list>
            </#if>
        </#list>
        </tbody>
    </table>
</div>

<br/>
<div><b>二、正常启用</b></div>
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
                应用名称
            </td>
            <td class="xl73" width="98"
                style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                状态监测
            </td>
            <td class="xl73" width="98"
                style="border: 0.5pt solid windowtext; width: 74pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                运行情况
            </td>
            <td class="xl73" width="90"
                style="border: 0.5pt solid windowtext; width: 67pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                运行信息
            </td>
            <td class="xl74" width="136"
                style="border: 0.5pt solid windowtext; width: 102pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                数据库
            </td>
            <td class="xl73" width="106"
                style="border: 0.5pt solid windowtext; width: 80pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                数据库监测
            </td>
            <td class="xl73" width="87"
                style="border: 0.5pt solid windowtext; width: 65pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                执行测试
            </td>
            <td class="xl73" width="92"
                style="border: 0.5pt solid windowtext; width: 69pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                连接情况
            </td>
            <td class="xl73" width="102"
                style="border: 0.5pt solid windowtext; width: 76pt; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                连接信息
            </td>
        </tr>
        <#list green as o>
        <tr height="18"
            style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; white-space: nowrap; text-align: right;">
            <td height="18" class="xl79" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="height: 13.2pt; border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; vertical-align: middle; white-space: nowrap; text-align: right;">${o.appServerDesc}</td><#--应用名称-->
            <td class="xl80" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.needCheck?string ("需要","不需要")}</td><#--状态监测-->
            <td class="xl80" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">
                <#if o.needCheck >
                     ${o.appRunning?string ("正常","未检测到应用")}
                 <#else>
                </#if>
            </td><#--运行情况-->
            <td class="xl80" rowspan="${o.dbConnectionList???then(o.dbConnectionList?size,1)}"
                style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${o.appRunningInfo???then(o.appRunningInfo,"")}</td><#--运行信息-->
            <#if (o.dbConnectionList?? && o.dbConnectionList?size > 0)>
                <#list o.dbConnectionList as db>
                    <#if (db_index = 0)>
                        <td class="xl81"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.dbType}
                            :${db.desc}</td><#--数据库-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.needCheck?string("需要","不需要")}</td><#--需要测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.testEd?string("已测试","未测试")}</td><#--执行测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.connectDB?string("正常","无法连接")}</td><#--连接情况-->
                        <td class="xl84"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.msg???string(db.msg,"")}</td> <#--连接信息-->
                    </#if>
                </#list>
            <#else>
                <td class="xl81"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--数据库-->
                <td class="xl82"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--需要测试-->
                <td class="xl82"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--执行测试-->
                <td class="xl82"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td><#--连接情况-->
                <td class="xl84"
                    style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;"></td> <#--连接信息-->
            </#if>
        </tr>
            <#if o.dbConnectionList??>
                <#list o.dbConnectionList as db>
                    <#if (db_index > 0)>
                    <tr height="18"
                        style="height:13.2pt;color: windowtext; font-size: 10pt; font-family: Arial, sans-serif; white-space: nowrap; text-align: right;">
                        <td class="xl81"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.dbType}
                            :${db.desc}</td><#--数据库-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.needCheck?string("需要","不需要")}</td><#--需要测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.testEd?string("已测试","未测试")}</td><#--执行测试-->
                        <td class="xl82"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.connectDB?string("正常","无法连接")}</td><#--连接情况-->
                        <td class="xl84"
                            style="border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;">${db.msg???string(db.msg,"")}</td> <#--连接信息-->
                    </tr>
                    </#if>
                </#list>
            </#if>
        </#list>
        </tbody>
    </table>
</div> <!-- 整体情况-->

<br/>
<br/>
<br/>
</body>
</html>