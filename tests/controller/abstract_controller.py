from time import sleep

from appium.webdriver.common.touch_action import TouchAction
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from selenium.common.exceptions import TimeoutException


class AbstractController(object):

    def __init__(self, driver):
        self.driver = driver

    def get_text(self, locator, idx=0):
        text = None
        if self.wait_for(*locator):
            elems = self.driver.find_elements(*locator)
            text = elems[idx].text
        return text

    def click_button(self, locator, idx=0):
        status = False
        if self.wait_for(*locator):
            buttons = self.driver.find_elements(*locator)
            buttons[idx].click()
            status = True

        return status

    def long_click(self, element, delay=2):
        sleep(delay)
        status = False
        if element:
            actions = TouchAction(self.driver)
            actions.long_press(element)
            actions.perform()
            status = True
        return status

    def is_visible(self, locator, delay=2):
        sleep(delay)
        status = False
        elems = self.driver.find_elements(*locator)
        if elems:
            status = elems[0].is_displayed()
        return status

    def wait_for(self, locator_class, locator_name):
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located((locator_class, locator_name)))
        except TimeoutException:
            return False
        return True
