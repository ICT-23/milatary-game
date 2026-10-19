export type Screen = 'title'|'profile'|'briefing'|'mission'|'pause'|'choice'|'debrief'|'campaign';
export type AvatarKey = 'ember'|'ranger'|'tech';
export type CivilianChoice = 'evacuate'|'secure-intel'|null;
export interface SaveGame {version:1; callsign:string; avatar:AvatarKey; completedMissions:number; bestScore:number; trust:number; intel:number; civilianChoice:CivilianChoice; movementSensitivity:number; soundEnabled:boolean; storyFlags:Record<string,boolean>; updatedAt:string}
export interface Point {x:number;y:number}
export interface Interactable {id:string;label:string;x:number;y:number;available:boolean;kind:'character'|'relay'|'evidence'|'civilian'|'extraction'}
