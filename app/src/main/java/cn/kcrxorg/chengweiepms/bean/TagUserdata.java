package cn.kcrxorg.chengweiepms.bean;

public class TagUserdata {
    UserTraceData[] userTraceData;
    TagEpcData tagEpcDatabak;
    Boolean[] userErrorData;

    public UserTraceData[] getUserTraceData() {
        return userTraceData;
    }

    public void setUserTraceData(UserTraceData[] userTraceData) {
        this.userTraceData = userTraceData;
    }

    public TagEpcData getTagEpcDatabak() {
        return tagEpcDatabak;
    }

    public void setTagEpcDatabak(TagEpcData tagEpcDatabak) {
        this.tagEpcDatabak = tagEpcDatabak;
    }

    public Boolean[] getUserErrorData() {
        return userErrorData;
    }

    public void setUserErrorData(Boolean[] userErrorData) {
        this.userErrorData = userErrorData;
    }
}
