package net.vivatcreative.review.handlers;

public enum State {
    BUILDING, SUBMITTED, GHOSTED, ACCEPTED, DENIED;

    public String toColor(){
        switch (this){
            case BUILDING:
                return "&3BUILDING";
            case SUBMITTED:
                return "&eSUBMITTED";
            case GHOSTED:
                return "&eGHOSTED";
            case ACCEPTED:
                return "&aACCEPTED";
            case DENIED:
                return "&cDENIED";
            default:
                return "&7UNKOWN";
        }
    }
}
