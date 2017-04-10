import pyrebase

class DatabaseManager():
    ## initialize the firebase and all
    def __init__(self):
        config = {
            "apiKey": "AIzaSyCoXlIlOlcldI2LdIy1WDmmRK7EmTARd1o",
            "authDomain": "servertesting-bfdf0.firebaseapp.com",
            "databaseURL": "https://servertesting-bfdf0.firebaseio.com/",
            "storageBucket": "gs://servertesting-bfdf0.appspot.com"
        }

        self._firebase = firebase = pyrebase.initialize_app(config)
        self._auth = firebase.auth()
        self._db = firebase.database()
        self._store = firebase.storage()
        self._currUser = None
        print("made dbm")


    def login(self, email, password):
        # attempt to log the user in
        try:
            self._currUser = self._auth.sign_in_with_email_and_password(email, password)
            info = self._auth.get_account_info(self._currUser['idToken'])
            print(info)
            return self._currUser
        except:
             print("fucked up login")
             return None

    def createnewuser(self, email, password):
        #try:
            print("Here")
            self._auth.create_user_with_email_and_password(email, password)
            user = self.login(email, password)
            value = {user['localId']: "value"};
            self._auth.send_email_verification(user['idToken'])
            self._db.child("users").set(value, self._currUser['idToken'])
            return user
        # except:
        #     print("bad create")
        #     return None

    def getData(self):
        return self._db.child().get(self._currUser['idToken']).val()

    def store(self, filename):
       helps = self._store.child("Maps/").put("__init__.py",self._currUser['idToken'])


if __name__ == '__main__':
    dbm = DatabaseManager()
    user = dbm.login("caleb.d.gum@gmail.com", "password")
    if user is None:
        user = dbm.createnewuser("caleb.d.gum@gmail.com", "password")
    dbm.store(None)