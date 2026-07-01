import type { AxiosResponse } from 'axios';
import apiClient from './index';

interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

export interface SpeechRecognizeResult {
  text: string;
}

const unwrap = <T>(response: AxiosResponse<BaseResponse<T>>, fallbackMessage: string): T => {
  const payload = response.data;
  if (payload?.code === 0 && payload.data !== undefined) {
    return payload.data;
  }
  throw new Error(payload?.message || fallbackMessage);
};

export const recognizeSpeech = async (audioBlob: Blob): Promise<SpeechRecognizeResult> => {
  const formData = new FormData();
  const ext = audioBlob.type.includes('mp4') ? 'mp4' : 'webm';
  formData.append('file', audioBlob, `speech.${ext}`);

  const response = await apiClient.post<BaseResponse<SpeechRecognizeResult>>(
    '/api/speech/recognize',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  );
  return unwrap(response, '语音识别失败');
};
