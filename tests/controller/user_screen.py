from time import sleep as wait
from selenium.webdriver.common.by import By
from controller.abstract_controller import AbstractController


class UserScreen(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.follow_button = (
            By.ID, "com.example.roverondo_mobile:id/otherUserFollowTextView")
        self.pencil_icon = (
            By.ID, "com.example.roverondo_mobile:id/currentUserPencilEditView")
        self.bio = (
            By.ID, "com.example.roverondo_mobile:id/currentUserBioTextView")
        self.gender = (
            By.ID,
            "com.example.roverondo_mobile:id/currentUserGenderDataTextView")
        self.city = (
            By.ID,
            "com.example.roverondo_mobile:id/currentUserCityDataTextView")
        self.weight = (
            By.ID,
            "com.example.roverondo_mobile:id/currentUserWeightDataTextView")
        self.universal_edit = (By.ID,
                               "com.example.roverondo_mobile:id/dialogUserProfileEditingTextInputEditText")
        self.save_image = (By.ID,
                           "com.example.roverondo_mobile:id/dialogUserProfileEditingSaveImageView")
        self.male_button = (By.ID,
                            "com.example.roverondo_mobile:id/dialogUserProfileGenderEditingMaleRelativeLayout")
        self.female_button = (By.ID,
                              "com.example.roverondo_mobile:id/dialogUserProfileGenderEditingFemaleTextView")

    def follow_user(self):
        if not self.wait_for(*self.follow_button):
            return False

        follow = self.driver.find_element(*self.follow_button)
        if follow.text == "Follow":
            follow.click()
            wait(2)
        return True

    def unfollow_user(self):
        if not self.wait_for(*self.follow_button):
            return False

        follow = self.driver.find_element(*self.follow_button)
        if follow.text == "Followed":
            follow.click()
            wait(2)
        return True

    def get_follow_button_text(self):
        return self.get_text(self.follow_button)

    def open_bio(self):
        return self.click_button(self.pencil_icon)

    def open_city(self):
        return self.click_button(self.city)

    def open_weight(self):
        return self.click_button(self.weight)

    def open_gender(self):
        return self.click_button(self.gender)

    def get_bio(self):
        return self.get_text(self.bio).replace(" ", "")

    def get_city(self):
        return self.get_text(self.city).replace(" ", "")

    def get_weight(self):
        return self.get_text(self.weight).replace(" ", "")

    def get_gender(self):
        return self.get_text(self.gender).replace(" ", "")

    def change_universal(self, bio):
        if not self.wait_for(*self.universal_edit):
            return False

        edit_field = self.driver.find_element(*self.universal_edit)
        edit_field.clear()
        edit_field.send_keys(bio)

    def click_save(self):
        status = self.click_button(self.save_image)
        wait(4)
        return status

    def change_gender(self, gender):
        if gender == "MALE":
            return self.click_button(self.male_button)
        elif gender == "FEMALE":
            return self.click_button(self.female_button)
        return False
