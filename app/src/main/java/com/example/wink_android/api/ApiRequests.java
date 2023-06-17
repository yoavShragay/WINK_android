package com.example.wink_android.api;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.wink_android.DB.Chat;
import com.example.wink_android.DB.ChatDao;
import com.example.wink_android.DB.User;
import com.example.wink_android.repository.ChatRepository;
import com.example.wink_android.requests.AddFriendCallback;
import com.example.wink_android.requests.BasicUserData;
import com.example.wink_android.requests.LoginRequest;
import com.example.wink_android.requests.MessageAnswer;
import com.example.wink_android.requests.MessageRequest;
import com.example.wink_android.requests.RegisterRequest;
import com.example.wink_android.requests.ServerAnswer;
import com.example.wink_android.requests.UserFriend;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiRequests {
    Retrofit retrofit;
 WebServiceAPI webServiceAPI;
 private ChatRepository repository;

//String.valueOf(R.string.BaseUrl)
public ApiRequests( ChatRepository repository){
this.repository=repository;
    retrofit = new Retrofit.Builder()
         .baseUrl("http://10.0.2.2:5000")
//                 .callbackExecutor(Executor.newSingleThread)
         .addConverterFactory(GsonConverterFactory.create())
         .build();
         webServiceAPI = retrofit.create(WebServiceAPI.class);
}
    public void getToken(String username, String password) {
        LoginRequest loginRequest=new LoginRequest(username,password);
        Call<ResponseBody> token = webServiceAPI.postToken(loginRequest);
//        Log.i("ApiRequests", token.toString());
        // You can enqueue the call to execute it asynchronously
        token.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String token = response.body().string();
                        repository.setToken("bearer "+token);
                        repository.setStatus("exist");
                        // Handle the token
                        Log.i("ApiRequests", "Token: " + token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    repository.setStatus("not exist");
                    // Handle error response
                    Log.e("ApiRequests", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                repository.setStatus("not exist");
                // Handle failure
                Log.e("ApiRequests", "Failure: " + t.getMessage());
            }
        });
    }

//    public void getToken( String username, String password) {
//        LoginRequest loginRequest=new LoginRequest(username,password);
//        Call<ResponseBody> tokenCall = webServiceAPI.postToken(loginRequest);
//        try {
//            Response<ResponseBody> response = tokenCall.execute();
//            if (response.isSuccessful()) {
//                ResponseBody token = response.body();
//                if (token != null) {
//                    repository.setStatus("exist");
//                    repository.setToken(String.valueOf(token));
////                    Log.i("ApiRequests", "friend id: " + token);
//                }
//            } else {
//                // Handle unsuccessful response
//                repository.setStatus("not exist");
//                Log.e("ApiRequests", "Request failed with code: " + response.code());
//            }
//        } catch (IOException e) {
//            // Handle failure
//            Log.e("ApiRequests", "Request failed: " + e.getMessage());
//        }
//    }
    public void getMyUserData(String username,String token) {
        Call<BasicUserData> userData = webServiceAPI.getUsersUsername(username,token);

        userData.enqueue(new Callback<BasicUserData>() {
            @Override
            public void onResponse(Call<BasicUserData> call, Response<BasicUserData> response) {
                if (response.isSuccessful()) {
                    BasicUserData userData = response.body();
                    if (userData != null) {
                        repository.getUserDao().insertUser(new User(userData.getUsername(),userData.getDisplayName(),userData.getProfilePic(),token));
                        repository.setStatus("success user details");
                        Log.i("ApiRequests getMyUserData", "Username: " + userData.getUsername());
                    }
                } else {
                    repository.setStatus("failed user details");
                    // Handle unsuccessful response
                    Log.e("ApiRequests getMyUserData", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicUserData> call, Throwable t) {
                // Handle failure
                repository.setStatus("failed user details");
                Log.e("ApiRequests getMyUserData", "Request failed: " + t.getMessage());
            }
        });

    }
    public void registerUser(RegisterRequest registerRequest) {
//        RegisterRequest registerRequest= new RegisterRequest(username,password,displayName,profilePic);
        Call<ResponseBody> answer = webServiceAPI.postUsers(registerRequest);

        answer.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {


                        Log.i("ApiRequests", "new user added");

                } else {
                    // Handle error response
                    Log.e("ApiRequests", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle failure
                Log.e("ApiRequests", "Failure: " + t.getMessage());
            }
        });
    }
    public void getFriends(String token) {
    Call <List<UserFriend>> chats = webServiceAPI.getChats(token);
        //        Call<BasicUserData> userData = webServiceAPI.getUsersUsername(username,token);

        chats.enqueue(new Callback<List<UserFriend>>() {
            @Override
            public void onResponse(Call<List<UserFriend>> call, Response<List<UserFriend>> response) {
                if (response.isSuccessful()) {
                    List<UserFriend> chats = response.body();
                    if (chats != null) {
                        Log.i("ApiRequests", "id: " + chats.get(2).getLastMessage().getContent());
                    }
                } else {
                    // Handle unsuccessful response
                    Log.e("ApiRequests", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<UserFriend>> call, Throwable t) {
                // Handle failure
                Log.e("ApiRequests", "Request failed: " + t.getMessage());
            }
        });


    }
//    public void addFriend( String name, String token) {
//
//        LoginRequest request = new LoginRequest(name);
//        Call<UserFriend> friendCall = webServiceAPI.postChats(request, token);
//
//        try {
//            Response<UserFriend> response = friendCall.execute();
//            if (response.isSuccessful()) {
//                UserFriend friend = response.body();
//                if (friend != null) {
//                    Chat chat = new Chat(friend.getUser().getUsername(), friend.getUser().getDisplayName(), friend.getUser().getProfilePic());
//                    repository.add(chat);
//                    repository.setStatus(response.code());
//                    Log.i("ApiRequests", "friend id: " + friend.getId());
//                }
//            } else {
//                // Handle unsuccessful response
//                repository.setStatus(response.code());
//                Log.e("ApiRequests", "Request failed with code: " + response.code());
//            }
//        } catch (IOException e) {
//            // Handle failure
//            Log.e("ApiRequests", "Request failed: " + e.getMessage());
//        }
//    }
//public void addFriend(String name, String token, AddFriendCallback callback) {
//    LoginRequest request = new LoginRequest(name);
//    Call<UserFriend> friendCall = webServiceAPI.postChats(request, token);
//
//    friendCall.enqueue(new Callback<UserFriend>() {
//        @Override
//        public void onResponse(Call<UserFriend> call, Response<UserFriend> response) {
//            if (response.isSuccessful()) {
//                UserFriend friend = response.body();
//                if (friend != null) {
////                    Chat chat = new Chat(friend.getUser().getUsername(), friend.getUser().getDisplayName(), friend.getUser().getProfilePic());
////                    repository.add(chat);
////                    repository.setStatus(response.code());
//                    Log.i("ApiRequests", "friend id: " + friend.getId());
//                    callback.onSuccess(friend);
//                }
//            } else {
//                // Handle unsuccessful response
////                repository.setStatus(response.code());
//                Log.e("ApiRequests", "Request failed with code: " + response.code());
//                callback.onFailure(response.code(), "Request failed with code: " + response.code());
//            }
//        }

//        @Override
//        public void onFailure(Call<UserFriend> call, Throwable t) {
//            // Handle failure
//            Log.e("ApiRequests", "Request failed: " + t.getMessage());
//            callback.onFailure(0, "Request failed: " + t.getMessage());
//        }
//    });
//}

    public void addMessage(int id,String message,String token) {
        MessageRequest messageRequest=new MessageRequest(message);
//        Call<UserFriend> friendCall = webServiceAPI.postChats(request, token);
        Call<MessageAnswer> messageAnswerCall= webServiceAPI.postChatsIdMessages(id,token,messageRequest);
        messageAnswerCall.enqueue(new Callback<MessageAnswer>() {
            @Override
            public void onResponse(Call<MessageAnswer> call, Response<MessageAnswer> response) {
                if (response.isSuccessful()) {
                   MessageAnswer answer = response.body();
                    if (answer != null) {
                        Log.i("ApiRequests", "friend id: " + answer.getId());
                    }
                } else {
                    // Handle unsuccessful response
                    Log.e("ApiRequests", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageAnswer> call, Throwable t) {
                // Handle failure
                Log.e("ApiRequests", "Request failed: " + t.getMessage());
            }
        });
    }
    public void getMessagesFromApi(int id,String token) {
        Call <List<MessageAnswer>> messageAnswerCall= webServiceAPI.getChatsIdMessages(id,token);
        messageAnswerCall.enqueue(new Callback<List<MessageAnswer>>() {
            @Override
            public void onResponse(Call<List<MessageAnswer>> call, Response<List<MessageAnswer>> response) {
                if (response.isSuccessful()) {
                    List<MessageAnswer> answers = response.body();
                    if (answers != null) {
                        Log.i("ApiRequests", " id: " + answers.get(2).getId());
                    }
                } else {
                    // Handle unsuccessful response
                    Log.e("ApiRequests", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MessageAnswer>>call, Throwable t) {
                // Handle failure
                Log.e("ApiRequests", "Request failed: " + t.getMessage());
            }
        });
    }
}


