package com.electronics.invento.wechatp2p;

public class Users {
    String username, phone, bio, userid, usertype;
    byte[] profilebyte;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public byte[] getProfilebyte() {
        return profilebyte;
    }

    public void setProfilebyte(byte[] profilebyte) {
        this.profilebyte = profilebyte;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
}