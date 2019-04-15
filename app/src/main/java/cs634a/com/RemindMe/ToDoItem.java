package cs634a.com.RemindMe;


public class ToDoItem {
    private String content;
    private String address;
    private Boolean done;
    private String reminderDate;
    private Boolean hasReminder;

    public ToDoItem(String content, String address, Boolean done, String reminderDate, Boolean hasReminder) {
        this.content = content;
        this.address = address;
        this.done = done;
        this.reminderDate = reminderDate;
        this.hasReminder = hasReminder;
    }

    public Boolean getHasReminder() {
        return hasReminder;
    }

    public String getContent() {
        return content;
    }

    public String getAddress(){
        return address;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setAddress(String address) {this.address = address; }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public String getReminderDate() {
        return reminderDate;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ToDoItem other = (ToDoItem) obj;
        if (!content.equals(other.content))
            return false;
        if (!address.equals(other.address))
            return false;
        if (!reminderDate.equals(other.reminderDate))
            return false;
        if (hasReminder != other.hasReminder)
            return false;
        if (done != other.done)
            return false;
        return true;
    }
}
