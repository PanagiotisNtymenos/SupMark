package com.supmark;

public class ListItem {
    String listName;
    String listID;

    public String getListName() {
        return listName;
    }

    public void setListName(String supermarket) {
        this.listName = listName;
    }

    public String getListID() {
        return listID;
    }

    public void setListID(String listID) {
        this.listID = listID;
    }

    public ListItem(String listName, String listID) {
        this.listName = listName;
        this.listID = listID;
    }
}
