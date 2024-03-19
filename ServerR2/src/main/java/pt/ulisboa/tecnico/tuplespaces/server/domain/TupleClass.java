package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.List;
import java.util.ArrayList;

class Tuple {
    private String data;
    private boolean locked = false;
    private int clientId;

    public Tuple(String data) {
        this.data = data;
    }

    // Getters and setters
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
}