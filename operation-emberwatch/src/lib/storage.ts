import AsyncStorage from '@react-native-async-storage/async-storage';
import type {SaveGame} from '../types/game';
const KEY='@emberwatch/save-v1';
export const NEW_SAVE:SaveGame={version:1,callsign:'EMBER-07',avatar:'ember',completedMissions:0,bestScore:0,trust:0,intel:0,civilianChoice:null,movementSensitivity:1,soundEnabled:true,storyFlags:{},updatedAt:new Date(0).toISOString()};
export async function loadSave():Promise<SaveGame>{try{const raw=await AsyncStorage.getItem(KEY);if(!raw)return NEW_SAVE;const data=JSON.parse(raw) as Partial<SaveGame>;return {...NEW_SAVE,...data,storyFlags:{...NEW_SAVE.storyFlags,...(data.storyFlags??{})},movementSensitivity:clamp(data.movementSensitivity??1,.6,1.5)};}catch{return NEW_SAVE}}
export async function saveGame(save:SaveGame):Promise<void>{try{await AsyncStorage.setItem(KEY,JSON.stringify({...save,updatedAt:new Date().toISOString()}));}catch{/* Offline play remains available if device storage fails. */}}
export async function clearSave():Promise<void>{try{await AsyncStorage.removeItem(KEY)}catch{/* Session may still be reset locally. */}}
export function clamp(v:number,min:number,max:number){return Math.max(min,Math.min(max,v))}
