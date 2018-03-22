/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: G:\\git_project\\topwise_store\\src\\com\\topwise\\topos\\appstore\\api\\IDownloadStateListener.aidl
 */
package com.topwise.topos.appstore.api;
public interface IDownloadStateListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.topwise.topos.appstore.api.IDownloadStateListener
{
private static final java.lang.String DESCRIPTOR = "com.topwise.topos.appstore.api.IDownloadStateListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.topwise.topos.appstore.api.IDownloadStateListener interface,
 * generating a proxy if needed.
 */
public static com.topwise.topos.appstore.api.IDownloadStateListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.topwise.topos.appstore.api.IDownloadStateListener))) {
return ((com.topwise.topos.appstore.api.IDownloadStateListener)iin);
}
return new com.topwise.topos.appstore.api.IDownloadStateListener.Stub.Proxy(obj);
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
case TRANSACTION_onDownloadStateChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.onDownloadStateChanged(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onDownloadProgressChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.onDownloadProgressChanged(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onDownloadSpeedChanged:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.onDownloadSpeedChanged(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.topwise.topos.appstore.api.IDownloadStateListener
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
     * 下载状态
     * @param state 0 - 未下载，1 - 已下载，2 - 已安装，3 - 待更新，4 - 安装中，5 - 暂停下载，6 - 继续下载，7 - 取消下载
     */
@Override public void onDownloadStateChanged(java.lang.String id, int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_onDownloadStateChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 下载进度
     * @param progress 进度 1至100
     */
@Override public void onDownloadProgressChanged(java.lang.String id, int progress) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
_data.writeInt(progress);
mRemote.transact(Stub.TRANSACTION_onDownloadProgressChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 下载速度
     * @param speed 速度
     */
@Override public void onDownloadSpeedChanged(java.lang.String id, int speed) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
_data.writeInt(speed);
mRemote.transact(Stub.TRANSACTION_onDownloadSpeedChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onDownloadStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDownloadProgressChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onDownloadSpeedChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * 下载状态
     * @param state 0 - 未下载，1 - 已下载，2 - 已安装，3 - 待更新，4 - 安装中，5 - 暂停下载，6 - 继续下载，7 - 取消下载
     */
public void onDownloadStateChanged(java.lang.String id, int state) throws android.os.RemoteException;
/**
     * 下载进度
     * @param progress 进度 1至100
     */
public void onDownloadProgressChanged(java.lang.String id, int progress) throws android.os.RemoteException;
/**
     * 下载速度
     * @param speed 速度
     */
public void onDownloadSpeedChanged(java.lang.String id, int speed) throws android.os.RemoteException;
}
