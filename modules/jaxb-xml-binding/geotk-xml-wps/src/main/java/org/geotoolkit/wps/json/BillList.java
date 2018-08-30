/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2018, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.wps.json;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * BillList
 */
public class BillList implements WPSJSONResponse {

    private List<String> bills = null;

    public BillList() {

    }

    public BillList(List<String> bills) {
        this.bills = bills;
    }

    public BillList bills(List<String> bills) {
        this.bills = bills;
        return this;
    }

    public BillList addBillsItem(String billsItem) {

        if (this.bills == null) {
            this.bills = new ArrayList<>();
        }

        this.bills.add(billsItem);
        return this;
    }

    /**
     * Get bills
     *
     * @return bills
     *
     */
    public List<String> getBills() {
        return bills;
    }

    public void setBills(List<String> bills) {
        this.bills = bills;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BillList billList = (BillList) o;
        return Objects.equals(this.bills, billList.bills);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(bills);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BillList {\n");

        sb.append("    bills: ").append(toIndentedString(bills)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
