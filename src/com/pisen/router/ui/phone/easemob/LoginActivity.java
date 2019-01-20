/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pisen.router.ui.phone.easemob;

//import com.easemob.EMCallBack;
//import com.easemob.EMError;

/**
 * 登陆页面
 *
 */
public class LoginActivity{ /*extends BaseActivity {
    private boolean progressShow;
    private LoadingDialog progressDialog;
    private int selectedIndex = Constant.INTENT_CODE_IMG_SELECTED_DEFAULT;
    private int messageToIndex = Constant.MESSAGE_TO_DEFAULT;


    class HttpResult {
        boolean IsError;
        int ErrCode;
        String ErrMsg;
        String DetailError;
        EaseUser User;
    }

    class EaseUser {
        int Id;
        String Account;
        String UserName;
        String Password;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        selectedIndex = intent.getIntExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY, Constant.INTENT_CODE_IMG_SELECTED_DEFAULT);
        messageToIndex = intent.getIntExtra(Constant.MESSAGE_TO_INTENT_EXTRA, Constant.MESSAGE_TO_DEFAULT);
        UserInfoDto userInfo = CloudApplication.userInfo;
        String userName = EasemobUtils2Pisen.getLastUserName();
        if (!userInfo.Phone.equals(userName)) {//need to get new ease account
            showDialog(getResources().getString(R.string.is_contact_customer));
            VolleyManager.getInstance().post(HttpKeys.ACCOUNT_URL, AccountApiConfig.getEaseMobAccountJson(userInfo.Phone), new VolleyManager.HttpRequestLisenter() {

                @Override
                public void onHttpFinished(boolean success, String response) {
                    // TODO Auto-generated method stub
                    HttpResult result = GsonUtils.jsonDeserializer(response, HttpResult.class);
                    if (result != null) {
                        if (result.User != null) {
                            loginHuanxinServer(result.User.Account, result.User.Password);
                        } else {
                            UIHelper.showToastLong(LoginActivity.this, result.ErrMsg);
                            finish();
                        }
                    } else {
                        UIHelper.showToastLong(LoginActivity.this, "联系客服出错");
                        finish();
                    }
                }
            });
        } else {
            if (EMChat.getInstance().isLoggedIn()) {
                showDialog(getResources().getString(
                        R.string.is_contact_customer));
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            EMChatManager.getInstance()
                                    .loadAllConversations();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        toChatActivity();
                    }
                }).start();
            } else {
                String pwd = EasemobUtils2Pisen.getLastUserPwd();
                loginHuanxinServer(userName, pwd);
//				createRandomAccountAndLoginChatServer();
            }
        }
    }


    public void createRandomAccountAndLoginChatServer() {
        // 自动生成账号
        final String randomAccount = CommonUtils.getRandomAccount()Constant.DEFAULT_COSTOMER_ACCOUNT;
        final String userPwd = Constant.DEFAULT_ACCOUNT_PWD;
        showDialog(getResources().getString(R.string.is_contact_customer));
        createAccountToServer(randomAccount, userPwd, new EMCallBack() {

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loginHuanxinServer(randomAccount, userPwd);
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int errorCode, final String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (!LoginActivity.this.isFinishing()) {
                            progressDialog.dismiss();
                        }
                        String msg;
                        if (errorCode == EMError.NONETWORK_ERROR) {
                            msg = "网络不可用";
                        } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                            msg = "用户已存在";
                        } else if (errorCode == EMError.UNAUTHORIZED) {
                            msg = "无开放注册权限";
                        } else if (errorCode == EMError.ILLEGAL_USER_NAME) {
                            msg = "用户名非法";
                        } else {
                            msg = "注册失败：" + message;
                        }
                        UIHelper.showToast(LoginActivity.this, msg);
                        finish();
                    }
                });
            }
        });
    }


    private void createAccountToServer(final String uname, final String pwd, final EMCallBack callback) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    EMChatManager.getInstance().createAccountOnServer(uname, pwd);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (EaseMobException e) {
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    private LoadingDialog getProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new LoadingDialog(LoginActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    progressShow = false;
                    finish();
                }
            });
        }
        return progressDialog;
    }


    public void loginHuanxinServer(final String uname, final String upwd) {
        progressShow = true;
        showDialog(getResources().getString(R.string.is_contact_customer));
        // login huanxin server
        EMChatManager.getInstance().login(uname, upwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                if (!progressShow) {
                    return;
                }
                HXSDKHelper.getInstance().setHXId(uname);
                HXSDKHelper.getInstance().setPassword(upwd);
                try {
                    EMChatManager.getInstance().loadAllConversations();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                toChatActivity();
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {
                if (!progressShow) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        UIHelper.showToast(LoginActivity.this,
                                getResources().getString(R.string.is_contact_customer_failure_seconed) + message);
                        finish();
                    }
                });
            }
        });
    }

    private void showDialog(String msg) {
        progressDialog = getProgressDialog();
        progressDialog.setTitle(msg);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void toChatActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!LoginActivity.this.isFinishing())
                    progressDialog.dismiss();
                // 进入主页面
                startActivity(new Intent(LoginActivity.this, ChatActivity.class)
                        .putExtra(Constant.INTENT_CODE_IMG_SELECTED_KEY, selectedIndex).putExtra(Constant.MESSAGE_TO_INTENT_EXTRA, messageToIndex));
                finish();
            }
        });
    }

*/}
