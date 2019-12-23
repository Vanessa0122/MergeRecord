class Person {
    String ID;
    String name;
    String occupation;
    String gender;
    String address;
    String phoneNumber;

    public Person(String ID, String name, String occupation, String gender, String address, String phoneNumber){
        this.ID = ID;
        this.name = name;
        this.occupation = occupation;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getID() {
        return this.ID;
    }

    public void setID(String ID){
        this.ID = ID;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getGender(){
        return this.gender;
    }

    public void setGender(String gender){
        this.gender = gender;
    }

    public String getOccupation(){
        return this.occupation;
    }

    public void setOccupation(String occupation){
        this.occupation = occupation;
    }

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
}
