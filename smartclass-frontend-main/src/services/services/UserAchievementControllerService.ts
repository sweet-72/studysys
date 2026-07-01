/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_object_ } from '../models/BaseResponse_object_';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class UserAchievementControllerService {
  /**
   * getUserAchievements
   * @param userId userId
   * @returns BaseResponse_object_ OK
   * @throws ApiError
   */
  public static getUserAchievementsUsingGet(
    userId?: number,
  ): CancelablePromise<BaseResponse_object_> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/user/achievement/list',
      query: {
        userId,
      },
      errors: {
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
      },
    });
  }

  /**
   * getUserAchievementStats
   * @param userId userId
   * @returns BaseResponse_object_ OK
   * @throws ApiError
   */
  public static getUserAchievementStatsUsingGet(
    userId?: number,
  ): CancelablePromise<BaseResponse_object_> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/user/achievement/stats',
      query: {
        userId,
      },
      errors: {
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
      },
    });
  }

  /**
   * claimAchievementReward
   * @param userId userId
   * @param achievementId achievementId
   * @returns BaseResponse_object_ OK
   * @throws ApiError
   */
  public static claimAchievementRewardUsingPost(
    userId?: number,
    achievementId?: number,
  ): CancelablePromise<BaseResponse_object_> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/user/achievement/claim',
      query: {
        userId,
        achievementId,
      },
      errors: {
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
      },
    });
  }
}
