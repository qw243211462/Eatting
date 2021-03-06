package dawizards.eatting.bean;


import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;

/*
 * Created by WQH on 2015/10/23 14:55.
 */
public class Food extends BmobObject {
    public String name;
    public Float price;
    public String belongSchool;
    public String belongCanteen;
    public String imageUrl;

    List<User> attendPeople = new ArrayList<>(); //预定菜品的人数
    List<User> likePeople = new ArrayList<>();   //点赞人数


    /*
     * Operate on attendPeople and likePeople.
     */
    public void removeAttend(User aUser) {
        attendPeople.remove(aUser);
    }

    public void addAttend(User aUser) {
        attendPeople.add(aUser);
    }

    public int getLikePeopleNum() {
        return likePeople.size();
    }

    public int getAttendPeopleNum() {
        return attendPeople.size();
    }

    public boolean isAttend(User aUser) {
        return attendPeople.contains(aUser);
    }


    public boolean isLike(User aUser) {
        return likePeople.contains(aUser);
    }

    public void addLike(User aUser) {
        likePeople.add(aUser);
    }

    public void release() {
        attendPeople.clear();
        likePeople.clear();
    }

    /*
     * Convert the class between Food.class and FoodDB.class.
     * Because of Food is extend BmobObject that can not assign the primary key in Database.
     */
    public FoodDB convert() {
        FoodDB foodDB = new FoodDB();
        foodDB.id = this.getObjectId();
        foodDB.name = this.name;
        foodDB.price = this.price;
        foodDB.belongSchool = this.belongSchool;
        foodDB.belongCanteen = this.belongCanteen;
        foodDB.imageUrl = this.imageUrl;
        return foodDB;
    }

    public Food convert(FoodDB foodDB) {
        this.setObjectId(foodDB.id);
        this.name = foodDB.name;
        this.price = foodDB.price;
        this.belongSchool = foodDB.belongSchool;
        this.belongCanteen = foodDB.belongCanteen;
        this.imageUrl = foodDB.imageUrl;
        return this;
    }


    @Override
    public boolean equals(Object other) {
        return other instanceof Food && ((Food) other).getObjectId().equals(getObjectId());
    }

}
