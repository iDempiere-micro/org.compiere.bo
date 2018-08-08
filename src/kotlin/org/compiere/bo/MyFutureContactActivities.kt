package org.compiere.bo

import org.compiere.crm.SvrProcessBase
import org.compiere.crm.SvrProcessBaseSql
import org.compiere.model.I_C_BPartner
import org.compiere.model.I_C_ContactActivity
import org.compiere.orm.DefaultModelFactory
import org.compiere.orm.IModelFactory
import org.compiere.process.SvrProcess
import org.idempiere.common.util.DB
import org.idempiere.common.util.Env
import java.io.Serializable
import java.math.BigDecimal
import java.sql.Connection

data class MyFutureContactActivitiesResult(val activities: MutableList<I_C_ContactActivity>) : java.io.Serializable

class MyFutureContactActivities : SvrProcessBaseSql() {
    override val isRO: Boolean
        get() = true

    override fun getSqlResult(cnn: Connection): Serializable {
        val sql =
            """
        select
        * from adempiere.v_contactactivity where
        EXTRACT(WEEK FROM startdate) = EXTRACT(WEEK FROM current_date )
        and salesrep_id = ?
        and ad_client_id IN (0, ?) and ( ad_org_id IN (0,?) or ? = 0) and isactive = 'Y'
""".trimIndent()

        val statement = cnn.prepareStatement(sql)
        statement.setInt(1, AD_USER_ID)
        statement.setInt(2, AD_CLIENT_ID)
        statement.setInt(3, AD_ORG_ID)
        statement.setInt(4, AD_ORG_ID)

        val rs = statement.executeQuery()

        val modelFactory: IModelFactory = DefaultModelFactory()
        val activities = mutableListOf<I_C_ContactActivity>()

        while (rs.next()) {
            val c_contactactivity_id = rs.getObject("c_contactactivity_id") as BigDecimal?
            if (c_contactactivity_id != null) {
                val activity = modelFactory.getPO("C_ContactActivity", rs, "pokus") as I_C_ContactActivity
                activities.add(activity)
            }
        }

        return MyFutureContactActivitiesResult(activities)
    }
}