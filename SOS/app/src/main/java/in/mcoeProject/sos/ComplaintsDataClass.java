//Model class for user complaints.

package in.mcoeProject.sos;

public class ComplaintsDataClass {

    public String copName, copId, copPhone, complaintCreatedDate, complaintCreatedTime, complaintId, copProfileUrl;

    public ComplaintsDataClass(String copName, String copId, String copPhone, String complaintCreatedDate, String complaintCreatedTime, String complaintId, String copProfileUrl) {
        this.copName = copName;
        this.copId = copId;
        this.copPhone = copPhone;
        this.complaintCreatedDate = complaintCreatedDate;
        this.complaintCreatedTime = complaintCreatedTime;
        this.complaintId = complaintId;
        this.copProfileUrl = copProfileUrl;
    }

    //Getter methods for obtaining the class data members.
    public String getCopName() {
        return copName;
    }

    public String getCopId() {
        return copId;
    }

    public String getCopPhone() {
        return copPhone;
    }

    public String getComplaintCreatedDate() {
        return complaintCreatedDate;
    }

    public String getComplaintCreatedTime() {
        return complaintCreatedTime;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public String getCopProfileUrl() {
        return copProfileUrl;
    }
}
