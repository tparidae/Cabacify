package arithmeticcoding;

public interface ContextModel {

    public int copyState();

    public void updateLPS();

    public void updateMPS();

    public int getState();

    public byte getMPS();
    
    
    public void setLPSMode(int updateStandard);
}
