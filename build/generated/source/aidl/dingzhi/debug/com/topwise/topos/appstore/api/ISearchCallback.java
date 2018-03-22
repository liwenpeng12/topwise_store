/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: G:\\git_project\\topwise_store\\src\\com\\topwise\\topos\\appstore\\api\\ISearchCallback.aidl
 */
package com.topwise.topos.appstore.api;
public interface ISearchCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.topwise.topos.appstore.api.ISearchCallback
{
private static final java.lang.String DESCRIPTOR = "com.topwise.topos.appstore.api.ISearchCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.topwise.topos.appstore.api.ISearchCallback interface,
 * generating a proxy if needed.
 */
public static com.topwise.topos.appstore.api.ISearchCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.topwise.topos.appstore.api.ISearchCallback))) {
return ((com.topwise.topos.appstore.api.ISearchCallback)iin);
}
return new com.topwise.topos.appstore.api.ISearchCallback.Stub.Proxy(obj);
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
case TRANSACTION_onSearchSuccess:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onSearchSuccess(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onSearchFail:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onSearchFail(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.topwise.topos.appstore.api.ISearchCallback
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
     * 搜索成功
     * @param json 应用列表
     */
@Override public void onSearchSuccess(java.lang.String json) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(json);
mRemote.transact(Stub.TRANSACTION_onSearchSuccess, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 搜索失败
     * @param error 失败原因
     */
@Override public void onSearchFail(java.lang.String error) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(error);
mRemote.transact(Stub.TRANSACTION_onSearchFail, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSearchSuccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onSearchFail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * 搜索成功
     * @param json 应用列表
     */
public void onSearchSuccess(java.lang.String json) throws android.os.RemoteException;
/**
     * 搜索失败
     * @param error 失败原因
     */
public void onSearchFail(java.lang.String error) throws android.os.RemoteException;
}
