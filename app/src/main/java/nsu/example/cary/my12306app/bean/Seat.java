package nsu.example.cary.my12306app.bean;

import java.io.Serializable;

public class Seat implements Serializable {
    private String seatName;
    private int seatNum;    //数量
    private String seatPrice;
    private String seatNo;  //座位号

    @Override
    public String toString() {
        return "Seat{" +
                "seatName='" + seatName + '\'' +
                ", seatNum=" + seatNum +
                ", seatPrice='" + seatPrice + '\'' +
                ", seatNo='" + seatNo + '\'' +
                '}';
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public String getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(String seatPrice) {
        this.seatPrice = seatPrice;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }
}
