package com.mnao.mfp.cr.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IssueType {

    public Map<String, List<String>> getIssuesByCategory() {

        Map<String, List<String>> issues = new HashMap<>();
        issues.put("sales", Arrays.asList("Annual Business Plan-0",
                "Co-op Sales",
                "CPO/Remarketing",
                "CX360 Record Health",
                "CX360 Survey Health",
                "Dealer Financials",
                "Dealer Loyalty",
                "Dealer Risk Assessment",
                "Inventory & Ordering",
                "Lead Management",
                "Marketing",
                "MBEP Training",
                "MCVP",
                "MCVP Vechicle Expiring",
                "Other",
                "Owner Loyalty",
                "RDR information",
                "Registration Market Share",
                "Retail Sales",
                "Sales Customer Experience (MBEP 2.1 Index)",
                "SPI",
                "Training"));
        issues.put("after sales", Arrays.asList("Accessorry Business",
                "Annual Business Plan",
                "Co-op Service",
                "CX360 Record Health",
                "CX360 Survey Health",
                "Dealer Financials",
                "Dealer Risk Assestment",
                "FIRFT",
                "Marketing",
                "MBEP Training",
                "MCVP",
                "MCVP Vechicle Expiring",
                "Missed Recall Tiers",
                "MPC (PartsEye) Utilization",
                "Other",
                "Parts Purchase Loyalty",
                "Parts Sales",
                "Recalls",
                "Repair Orders",
                "Service Customer Experience (MBEP 2.1 Index)",
                "Service Retention/FYSL",
                "Shop Capacity",
                "Training",
                "X-Time Sevice Scheduling"));

        issues.put("network", Arrays.asList("Dealer Dev Deficiencies Identifed",
                "Dealer Staffing",
                "Facility",
                "NDAC Engagement",
                "Network Activity",
                "Other",
                "UMX"));

        return issues;
    }
}
