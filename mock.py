from flask import Flask, request, jsonify
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO, format='[%(asctime)s] %(levelname)s: %(message)s')
logger = logging.getLogger(__name__)

# In-memory user store
users = {
    "1": {"name": "John Doe", "email": "john.doe@test.com", "phoneNumber": "777777777", "userId": "1", "username": "user1", "password": "pass1", "pin": "1234"},
    "2": {"name": "Jane Doe", "email": "jane.doe@test.com", "phoneNumber": "888888888", "userId": "2", "username": "user2", "password": "pass2", "pin": "4321"}
}

def get_user_by_field(field, value):
    return next((user for user in users.values() if user.get(field) == value), {})

def get_user_by_multiple_fields(fields_dict):
    for user in users.values():
        match = True
        for field, value in fields_dict.items():
            if field == "providerName" and user.get("provider"):
                if user.get("provider", {}).get("name") != value:
                    match = False
                    break
            elif field == "providerUserId" and user.get("provider"):
                if user.get("provider", {}).get("providerUserId") != value:
                    match = False
                    break
            elif user.get(field) != value:
                match = False
                break
        if match:
            return user
    return {}

@app.route('/user', methods=['GET'])
def get_user():
    logger.info("GET %s", request.path)
    logger.info("Headers: %s", request.headers)
    logger.info("Query Params: %s", request.args)

    email = request.args.get("email")
    phone_number = request.args.get("phoneNumber")
    user_id = request.args.get("userId")
    username = request.args.get("username")
    provider_name = request.args.get("providerName")
    provider_user_id = request.args.get("providerUserId")

    search_criteria = {}
    
    if email:
        search_criteria["email"] = email
    if phone_number:
        search_criteria["phoneNumber"] = phone_number
    if user_id:
        search_criteria["userId"] = user_id
    if username:
        search_criteria["username"] = username
    if provider_name:
        search_criteria["providerName"] = provider_name
    if provider_user_id:
        search_criteria["providerUserId"] = provider_user_id

    if not search_criteria:
        return jsonify({"error": {"message": "Invalid request"}}), 400

    user = get_user_by_multiple_fields(search_criteria)
    
    if user:
        return jsonify(user), 200
    else:
        return jsonify({}), 200

@app.route('/user', methods=['POST'])
def create_user():
    data = request.json
    logger.info("POST /user with data: %s", data)

    username = data.get("username")
    password = data.get("password")
    pin = data.get("pin")
    phone = data.get("phoneNumber")
    email = data.get("email")
    provider = data.get("provider")

    if not ((username and (password or pin)) or phone or email or provider):
        return jsonify({"error": {"message": "Invalid request"}}), 400

    for field, message in [("username", "Username taken"), ("phoneNumber", "phone taken"), ("email", "email taken")]:
        if data.get(field) and get_user_by_field(field, data[field]).get("userId"):
            return jsonify({"error": {"message": message}}), 400

    user_id = str(len(users) + 1)
    new_user = {
        "userId": user_id,
        "username": username,
        "password": password,
        "pin": pin,
        "phoneNumber": phone,
        "email": email,
        "name": data.get("name"),
        "firstName": data.get("firstName"),
        "middleName": data.get("middleName"),
        "lastName": data.get("lastName"),
        "picture": data.get("picture")
    }
    
    if provider:
        new_user["provider"] = provider
    
    users[user_id] = new_user

    return jsonify(users[user_id]), 201

@app.route('/authenticateUser', methods=['POST'])
def authenticate_user():
    data = request.json
    logger.info("POST /authenticateUser with data: %s", data)

    username = data.get("username")
    phone_number = data.get("phoneNumber")
    email = data.get("email")
    password = data.get("password")
    pin = data.get("pin")

    # Validate exactly one identifier is provided
    identifiers = [username, phone_number, email]
    provided_identifiers = [i for i in identifiers if i is not None]
    if len(provided_identifiers) != 1:
        return jsonify({"error": {"message": "Exactly one identifier required"}}), 400

    # Validate exactly one credential is provided
    credentials = [password, pin]
    provided_credentials = [c for c in credentials if c is not None]
    if len(provided_credentials) != 1:
        return jsonify({"error": {"message": "Exactly one credential required"}}), 400

    # Find user by identifier
    user = None
    if username:
        user = get_user_by_field("username", username)
    elif phone_number:
        user = get_user_by_field("phoneNumber", phone_number)
    elif email:
        user = get_user_by_field("email", email)

    # Check if user exists
    if not user or not user.get("userId"):
        return jsonify({"error": {"message": "User does not exist"}}), 404

    # Validate credentials
    if password and user.get("password") != password:
        return jsonify({"error": {"message": "Unauthorized"}}), 401
    elif pin and user.get("pin") != pin:
        return jsonify({"error": {"message": "Unauthorized"}}), 401

    return jsonify(user), 200

@app.route('/provider', methods=['POST'])
def post_provider():
    data = request.json
    logger.info("POST /provider with data: %s", data)

    user_id, provider = data.get("userId"), data.get("provider")
    if not user_id or not provider or user_id not in users:
        return jsonify({"error": {"message": "Invalid request"}}), 400

    users[user_id]["provider"] = provider
    return jsonify({}), 200

@app.route('/sendEmail', methods=['POST'])
@app.route('/sendSms', methods=['POST'])
def notify():
    logger.info("POST %s", request.path)
    logger.info("Headers: %s", request.headers)
    logger.info("Payload: %s", request.json)
    return jsonify({}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6000, debug=True)
