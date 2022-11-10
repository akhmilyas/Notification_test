"""Server Side FCM sample.
Firebase Cloud Messaging (FCM) can be used to send messages to clients on iOS,
Android and Web.
This sample uses FCM to send two types of messages to clients that are subscribed
to the `news` topic. One type of message is a simple notification message (display message).
The other is a notification message (display notification) with platform specific
customizations. For example, a badge is added to messages that are sent to iOS devices.
"""

import os
import json
import requests

PROJECT_ID = 'vkc-mobile'
BASE_URL = 'https://fcm.googleapis.com'
FCM_ENDPOINT = 'v1/projects/' + PROJECT_ID + '/messages:send'
FCM_URL = 'https://fcm.googleapis.com/fcm/send'
SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']
BASE_DIR = os.path.dirname(os.path.abspath(__file__)) + '/'


# [START retrieve_access_token]
def _get_access_token():
    """Retrieve a valid access token that can be used to authorize requests.
    :return: Access token.
    """
    with open(BASE_DIR + 'fcm_project_token', 'r') as file:
        token = file.read().rstrip()
    return token


# [END retrieve_access_token]

def _send_fcm_message(fcm_message):
    """Send HTTP request to FCM with given message.
    Args:
      fcm_message: JSON object that will make up the body of the request.
    """
    headers = {
        'Authorization': 'key=' + _get_access_token(),
        'Content-Type': 'application/json; UTF-8',
    }
    resp = requests.post(FCM_URL, data=json.dumps(fcm_message), headers=headers)

    if resp.status_code == 200:
        print('Message sent to Firebase for delivery, response:')
        print(resp.text)
    else:
        print('Unable to send message to Firebase')
        print(resp.text)


def _build_common_message(token):
    """Construct common notifiation message.
    Construct a JSON object that will be used to define the
    common parts of a notification message that will be sent
    to any app instance subscribed to the news topic.
    """
    return {
        'data': {
            'name': 'UserName Notification',
            'id': 'asdfg123',
            'slug': 'user4',
            'eventName': 'user4',
            'eventId': '7e403c00-93b3-4ff6-9864-3f23240ba3af',
        },
        'priority': 'high',
        'to': token,
    }


def main():
    with open(BASE_DIR + 'user_token', 'r') as file:
        token = file.read().rstrip()
    if token:
        common_message = _build_common_message(token)
        print('FCM request body for message using common notification object:')
        print(json.dumps(common_message, indent=2))
        _send_fcm_message(common_message)
    else:
        print('''Invalid user_token file, please write down plain key there''')


if __name__ == '__main__':
    main()
