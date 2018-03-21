/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\SourceTree_workspace\\AppStore\\src\\com\\ibimuyu\\appstore\\api\\IAppStoreApi.aidl
 */
package com.topwise.topos.appstore.api;
public interface IAppStoreApi extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.topwise.topos.appstore.api.IAppStoreApi
{
private static final java.lang.String DESCRIPTOR = "com.topwise.topos.appstore.api.IAppStoreApi";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.topwise.topos.appstore.api.IAppStoreApi interface,
 * generating a proxy if needed.
 */
public static com.topwise.topos.appstore.api.IAppStoreApi asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.topwise.topos.appstore.api.IAppStoreApi))) {
return ((com.topwise.topos.appstore.api.IAppStoreApi)iin);
}
return new com.topwise.topos.appstore.api.IAppStoreApi.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_searchApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.topwise.topos.appstore.api.ISearchCallback _arg1;
_arg1 = com.topwise.topos.appstore.api.ISearchCallback.Stub.asInterface(data.readStrongBinder());
this.searchApp(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_startDownloadApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.topwise.topos.appstore.api.IDownloadStateListener _arg1;
_arg1 = com.topwise.topos.appstore.api.IDownloadStateListener.Stub.asInterface(data.readStrongBinder());
boolean _result = this.startDownloadApp(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_cancelDownloadApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.cancelDownloadApp(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_resumeDownloadApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.resumeDownloadApp(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_pauseDownloadApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.pauseDownloadApp(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getAppState:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getAppState(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getAppUpgradeDesc:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getAppUpgradeDesc(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.topwise.topos.appstore.api.IAppStoreApi
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * 搜索应用
     * @param keyword 搜索关键字
     * @param callback 搜索结果回调
     */
@Override public void searchApp(java.lang.String keyword, com.topwise.topos.appstore.api.ISearchCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(keyword);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_searchApp, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 开始下载应用
     * @param id 应用id（即应用包名）
     * @param IDownloadStateListener 下载状态监听
     * @return 是否成功开始下载
     */
@Override public boolean startDownloadApp(java.lang.String id, com.topwise.topos.appstore.api.IDownloadStateListener l) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
_data.writeStrongBinder((((l!=null))?(l.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_startDownloadApp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 取消下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功取消下载
     */
@Override public boolean cancelDownloadApp(java.lang.String id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
mRemote.transact(Stub.TRANSACTION_cancelDownloadApp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 继续下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功继续下载
     */
@Override public boolean resumeDownloadApp(java.lang.String id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
mRemote.transact(Stub.TRANSACTION_resumeDownloadApp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 暂停下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功暂停下载
     */
@Override public boolean pauseDownloadApp(java.lang.String id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
mRemote.transact(Stub.TRANSACTION_pauseDownloadApp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getAppState(java.lang.String id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
mRemote.transact(Stub.TRANSACTION_getAppState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 获取当前应用升级描述
     * @param id 应用id（即应用包名）
     * @return 应用新版本描述
     */
@Override public java.lang.String getAppUpgradeDesc(java.lang.String id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
mRemote.transact(Stub.TRANSACTION_getAppUpgradeDesc, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_searchApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startDownloadApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_cancelDownloadApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_resumeDownloadApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_pauseDownloadApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getAppState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getAppUpgradeDesc = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
/**
     * 搜索应用
     * @param keyword 搜索关键字
     * @param callback 搜索结果回调
     */
public void searchApp(java.lang.String keyword, com.topwise.topos.appstore.api.ISearchCallback callback) throws android.os.RemoteException;
/**
     * 开始下载应用
     * @param id 应用id（即应用包名）
     * @param IDownloadStateListener 下载状态监听
     * @return 是否成功开始下载
     */
public boolean startDownloadApp(java.lang.String id, com.topwise.topos.appstore.api.IDownloadStateListener l) throws android.os.RemoteException;
/**
     * 取消下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功取消下载
     */
public boolean cancelDownloadApp(java.lang.String id) throws android.os.RemoteException;
/**
     * 继续下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功继续下载
     */
public boolean resumeDownloadApp(java.lang.String id) throws android.os.RemoteException;
/**
     * 暂停下载应用
     * @param id 应用id（即应用包名）
     * @return 是否成功暂停下载
     */
public boolean pauseDownloadApp(java.lang.String id) throws android.os.RemoteException;
public int getAppState(java.lang.String id) throws android.os.RemoteException;
/**
     * 获取当前应用升级描述
     * @param id 应用id（即应用包名）
     * @return 应用新版本描述
     */
public java.lang.String getAppUpgradeDesc(java.lang.String id) throws android.os.RemoteException;
}
