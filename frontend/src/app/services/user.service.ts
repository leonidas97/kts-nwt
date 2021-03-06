import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from '../../app/models/user';
import { PasswordEdit } from '../../app/models/password-edit.model';
import { EditProfileInfo } from '../../app/models/profile-edit-info.model';
import { Results } from '../models/results';

@Injectable({ providedIn: 'root' })
export class UserService {

    constructor(private http: HttpClient) { }

    register(user: User) {
        return this.http.post('api/users/register', user);
    }

    searchAdmins(firstname = '', lastname = '', email = '', pageNum = 0, pageSize = 5): Observable<Results> {
      let params = new HttpParams();
      params = params.append('firstname', firstname);
      params = params.append('lastname', lastname);
      params = params.append('email', email);
      params = params.append('pageNum', pageNum.toString());
      params = params.append('pageSize', pageSize.toString());

      return this.http.get<Results>('api/users/admins', {params});
    }

    createAdmin(admin: User) {
      return this.http.post('api/users/admins', admin);
    }

    deleteAdmin(id: number) {
      return this.http.delete(`api/users/admins/${id}`);
    }

    getAllUsers(): Observable<Results> {
      return this.http.get<Results>('api/users');
    }

    searchUsers(firstname: string, lastname: string, email: string): Observable<Results> {
      let params = new HttpParams();
      params = params.append('firstname', firstname);
      params = params.append('lastname', lastname);
      params = params.append('email', email);
      return this.http.get<Results>('api/users', {params});
    }

    banUser(user: User) {
      return this.http.put(`api/users/ban/${user.id}`, user);
    }

    unBanUser(user: User) {
      return this.http.put(`api/users/unban/${user.id}`, user);
    }

    changePassword(passwords: PasswordEdit) {
      return this.http.post('api/auth/change_password', passwords);
    }

    changeMyInfo(editInfoData: EditProfileInfo) {
      return this.http.put('api/users', editInfoData);
    }
}
