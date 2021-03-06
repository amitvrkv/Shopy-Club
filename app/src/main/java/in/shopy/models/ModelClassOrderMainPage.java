package in.shopy.models;

/**
 * Created by am361000 on 28/11/17.
 */

public class ModelClassOrderMainPage {
    String orderid;
    String date;
    String total;
    String deliverycharge;
    String discount;
    String grandtotal;
    String status;
    String comment;
    String paymentmode;

    public ModelClassOrderMainPage() {
    }

    public ModelClassOrderMainPage(String orderid, String date, String total, String deliverycharge, String discount, String grandtotal, String status, String comment, String paymentmode) {
        this.orderid = orderid;
        this.date = date;
        this.total = total;
        this.deliverycharge = deliverycharge;
        this.discount = discount;
        this.grandtotal = grandtotal;
        this.status = status;
        this.comment = comment;
        this.paymentmode = paymentmode;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDeliverycharge() {
        return deliverycharge;
    }

    public void setDeliverycharge(String deliverycharge) {
        this.deliverycharge = deliverycharge;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getGrandtotal() {
        return grandtotal;
    }

    public void setGrandtotal(String grandtotal) {
        this.grandtotal = grandtotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPaymentmode() {
        return paymentmode;
    }

    public void setPaymentmode(String paymentmode) {
        this.paymentmode = paymentmode;
    }
}
